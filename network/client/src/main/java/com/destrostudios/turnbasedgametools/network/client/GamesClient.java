package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamesClient<S, A> {
    private final Client client;
    private final GameService<S, A> gameService;
    private final Map<UUID, ClientGameData<S, A>> games = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    public GamesClient(String host, int port, int timeout, GameService<S, A> service) throws IOException {
        gameService = service;
        client = new Client();
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
                if (object instanceof GameSpectateAck) {
                    GameSpectateAck message = (GameSpectateAck) object;
                    games.put(message.gameId, new ClientGameData<>(message.gameId, (S) message.state));
                } else if (object instanceof GameAction) {
                    GameAction message = (GameAction) object;
                    ClientGameData<S, A> game = games.get(message.gameId);
                    game.enqueueAction((A) message.action, message.randomHistory);
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

    public void startNewGame() {
        client.sendTCP(new GameStartRequest());
    }

    public void sendAction(UUID gameId, Object action) {
        client.sendTCP(new GameActionRequest(gameId, action));
    }

    public void spectate(UUID gameId) {
        client.sendTCP(new GameSpectateRequest(gameId));
    }

    public boolean updateGame(UUID id) {
        return getGame(id).update(gameService);
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

    public void stop() {
        client.stop();
    }
}
