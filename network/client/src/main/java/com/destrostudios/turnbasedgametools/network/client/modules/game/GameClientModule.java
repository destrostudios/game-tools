package com.destrostudios.turnbasedgametools.network.client.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.GameService;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameJoinAck;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameJoinRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameStartRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnsubscribeGamesList;
import com.esotericsoftware.kryonet.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameClientModule<S, A, P> extends GameModule<S, A, P> {

    private static final Logger LOG = LoggerFactory.getLogger(GameClientModule.class);

    private final Connection connection;
    private final Map<UUID, ClientGameData<S, A, P>> games = new ConcurrentHashMap<>();
    private final Set<UUID> gamesList = new CopyOnWriteArraySet<>();

    public GameClientModule(GameService<S, A, P> gameService, Connection connection) {
        super(gameService);
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof GameJoinAck) {
            GameJoinAck message = (GameJoinAck) object;
            onJoinGame(message.gameId, (S) message.state);
        } else if (object instanceof GameAction) {
            GameAction message = (GameAction) object;
            onAction(message.gameId, (A) message.action, message.randomHistory);
        } else if (object instanceof ListGame) {
            ListGame message = (ListGame) object;
            gamesList.add(message.gameId);
        } else if (object instanceof UnlistGame) {
            UnlistGame message = (UnlistGame) object;
            gamesList.remove(message.gameId);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        games.clear();
        gamesList.clear();
    }

    private void onJoinGame(UUID gameId, S gameState) {
        games.put(gameId, new ClientGameData<>(gameId, gameState));
    }

    private void onAction(UUID gameId, A action, int[] randomHistory) {
        ClientGameData<S, A, P> game = games.get(gameId);
        game.enqueueAction(action, randomHistory);
    }

    public void startNewGame(P params) {
        connection.sendTCP(new GameStartRequest(params));
    }

    public void sendAction(UUID gameId, Object action) {
        connection.sendTCP(new GameActionRequest(gameId, action));
    }

    public void join(UUID gameId) {
        connection.sendTCP(new GameJoinRequest(gameId));
    }

    public boolean applyNextAction(UUID id) {
        ClientGameData<S, A, P> game = getJoinedGame(id);
        if (game.isDesynced()) {
            return false;
        }
        try {
            return game.applyNextAction(gameService);
        } catch (Throwable t) {
            game.setDesynced();
            LOG.error("Game {} is likely desynced. Attempting to rejoin...", game.getId(), t);
            join(game.getId());
            return false;
        }
    }

    public boolean applyAllActions(UUID id) {
        ClientGameData<S, A, P> game = getJoinedGame(id);
        if (game.isDesynced()) {
            return false;
        }
        try {
            boolean updated = false;
            while (game.applyNextAction(gameService)) {
                updated = true;
            }
            return updated;
        } catch (Throwable t) {
            game.setDesynced();
            LOG.error("Game {} is likely desynced. Attempting to rejoin...", game.getId(), t);
            join(game.getId());
            return false;
        }
    }

    public void subscribeToGamesList() {
        connection.sendTCP(new SubscribeGamesList());
    }

    public void unsubscribeFromGamesList() {
        connection.sendTCP(new UnsubscribeGamesList());
    }

    public ClientGameData<S, A, P> getJoinedGame(UUID id) {
        return games.get(id);
    }

    public List<ClientGameData<S, A, P>> getJoinedGames() {
        return List.copyOf(games.values());
    }

    public Set<UUID> getGamesList() {
        return Set.copyOf(gamesList);
    }

}
