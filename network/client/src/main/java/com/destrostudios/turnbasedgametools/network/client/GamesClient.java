package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameJoinAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameJoinRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.messages.Pong;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamesClient<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(GamesClient.class);

    private final Client client;
    private final GameService<S, A> gameService;
    private final Map<UUID, ClientGameData<S, A>> games = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    public GamesClient(String host, int port, int timeout, GameService<S, A> service) throws IOException {
        gameService = service;
        client = new Client(10_000_000, 10_000_000);
        NetworkUtil.initialize(client.getKryo());
        gameService.initialize(client.getKryo());
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                for (Listener listener : listeners) {
                    listener.connected(connection);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                for (Listener listener : listeners) {
                    listener.disconnected(connection);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof GameJoinAck) {
                    GameJoinAck message = (GameJoinAck) object;
                    onJoinGame(message.gameId, (S) message.state);
                } else if (object instanceof GameAction) {
                    GameAction message = (GameAction) object;
                    onAction(message.gameId, (A) message.action, message.randomHistory);
                } else if (object instanceof Ping) {
                    connection.sendTCP(new Pong());
                }
                for (Listener listener : listeners) {
                    listener.received(connection, object);
                }
            }

            @Override
            public void idle(Connection connection) {
                for (Listener listener : listeners) {
                    listener.idle(connection);
                }
            }
        });

        client.start();
        client.connect(timeout, host, port);
    }

    private void onJoinGame(UUID gameId, S gameState) {
        games.put(gameId, new ClientGameData<>(gameId, gameState));
    }

    private void onAction(UUID gameId, A action, int[] randomHistory) {
        ClientGameData<S, A> game = games.get(gameId);
        game.enqueueAction(action, randomHistory);
    }

    public void startNewGame() {
        client.sendTCP(new GameStartRequest());
    }

    public void sendAction(UUID gameId, Object action) {
        client.sendTCP(new GameActionRequest(gameId, action));
    }

    public void join(UUID gameId) {
        client.sendTCP(new GameJoinRequest(gameId));
    }

    public boolean applyNextAction(UUID id) {
        ClientGameData<S, A> game = getGame(id);
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
        ClientGameData<S, A> game = getGame(id);
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

    public ClientGameData<S, A> getGame(UUID id) {
        return games.get(id);
    }

    public List<ClientGameData<S, A>> getGames() {
        return new ArrayList<>(games.values());
    }

    public GameService<S, A> getService() {
        return gameService;
    }

    public void addConnectionListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(Listener listener) {
        listeners.remove(listener);
    }

    public Client getKryoClient() {
        return client;
    }

    public void stop() {
        client.stop();
    }
}
