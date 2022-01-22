package com.destrostudios.gametools.network.client.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.GameModule;
import com.destrostudios.gametools.network.shared.modules.game.GameService;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameActionRequest;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoin;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoinRequest;
import com.esotericsoftware.kryonet.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameClientModule<S, A> extends GameModule<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(GameClientModule.class);

    private final Connection connection;
    private final Map<UUID, ClientGameData<S, A>> games = new ConcurrentHashMap<>();

    public GameClientModule(GameService<S, A> gameService, Connection connection) {
        super(gameService);
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof GameJoin) {
            GameJoin message = (GameJoin) object;
            onJoinGame(message.gameId, (S) message.state);
        } else if (object instanceof GameAction) {
            GameAction message = (GameAction) object;
            onAction(message.gameId, (A) message.action, message.randomHistory);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        games.clear();
    }

    private void onJoinGame(UUID gameId, S gameState) {
        games.put(gameId, new ClientGameData<>(gameId, gameState));
    }

    private void onAction(UUID gameId, A action, int[] randomHistory) {
        ClientGameData<S, A> game = games.get(gameId);
        game.offerAction(action, randomHistory);
    }

    public void sendAction(UUID gameId, Object action) {
        connection.sendTCP(new GameActionRequest(gameId, action));
    }

    public void join(UUID gameId) {
        connection.sendTCP(new GameJoinRequest(gameId));
    }

    public boolean applyNextAction(UUID id) {
        ClientGameData<S, A> game = getJoinedGame(id);
        if (game.isDesynced()) {
            return false;
        }
        try {
            if (game.applyNextAction(gameService)) {
                return true;
            }
        } catch (Throwable t) {
            LOG.error("Exception when handling action for game {}.", game.getId(), t);
        }
        if (game.isDesynced()) {
            LOG.info("Game {} is marked as desynced. Attempting to rejoin...", game.getId());
            join(game.getId());
        }
        return false;
    }

    public boolean applyAllActions(UUID id) {
        boolean updated = false;
        while (applyNextAction(id)) {
            updated = true;
        }
        ClientGameData<S, A> game = getJoinedGame(id);
        return updated && !game.isDesynced();
    }

    public void removeJoinedGame(UUID gameId) {
        games.remove(gameId);
    }

    public ClientGameData<S, A> getJoinedGame(UUID id) {
        return games.get(id);
    }

    public List<ClientGameData<S, A>> getJoinedGames() {
        return List.copyOf(games.values());
    }

}
