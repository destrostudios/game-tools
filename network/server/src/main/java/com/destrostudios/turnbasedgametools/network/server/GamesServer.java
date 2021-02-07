package com.destrostudios.turnbasedgametools.network.server;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameJoinAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameJoinRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.messages.Pong;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamesServer<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(GamesServer.class);

    private final Server server;
    private final GameService<S, A> gameService;
    private final Map<UUID, ServerGameData<S, A>> games = new ConcurrentHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    public GamesServer(int port, GameService<S, A> service) throws IOException {
        gameService = service;
        server = new Server();
        NetworkUtil.initialize(server.getKryo());
        gameService.initialize(server.getKryo());
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                for (Listener listener : listeners) {
                    listener.connected(connection);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                removeSpectator(connection.getID());
                for (Listener listener : listeners) {
                    listener.disconnected(connection);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                try {
                    handleReceivedMessage(connection, object);
                } catch (Throwable t) {
                    LOG.error("Exception when handling received message {} from connection {}.", object, connection.getID(), t);
                }
            }

            @Override
            public void idle(Connection connection) {
                for (Listener listener : listeners) {
                    listener.idle(connection);
                }
            }
        });
        server.start();
        server.bind(port);
    }

    private void handleReceivedMessage(Connection connection, Object object) {
        if (object instanceof GameJoinRequest) {
            GameJoinRequest message = (GameJoinRequest) object;
            join(connection, message.gameId, Collections.emptySet());
        } else if (object instanceof GameActionRequest) {
            GameActionRequest message = (GameActionRequest) object;
            applyAction(message.game, (A) message.action);
        } else if (object instanceof GameStartRequest) {
            UUID gameId = startNewGame();
            join(connection, gameId, Collections.emptySet());
        } else if (object instanceof Ping) {
            connection.sendTCP(new Pong());
        }
        for (Listener listener : listeners) {
            listener.received(connection, object);
        }
    }

    public UUID startNewGame() {
        UUID id = UUID.randomUUID();
        S state = gameService.startNewGame();
        games.put(id, new ServerGameData<>(id, state, new SecureRandom()));
        return id;
    }

    public void join(Connection connection, UUID gameId, Set<Object> tags) {
        ServerGameData<S, A> game = games.get(gameId);
        game.setConnectionTags(connection.getID(), tags);
        connection.sendTCP(new GameJoinAck(game.id, game.state, tags.toArray()));
    }

    public void applyAction(UUID gameId, A action) {
        ServerGameData<S, A> game = games.get(gameId);
        MasterRandom random = new MasterRandom(game.random);

        ByteArrayOutputStream backupOutputStream = new ByteArrayOutputStream();
        Kryo kryo = new Kryo();
        gameService.initialize(kryo);
        try (Output output = new Output(backupOutputStream)) {
            kryo.writeObject(output, game.state);
            output.flush();
        }
        try {
            game.state = gameService.applyAction(game.state, action, random);
        } catch (Throwable t) {
            LOG.warn("Game {} was rolled back due to an exception when trying to apply {}.", gameId, action);
            try (Input input = new Input(new ByteArrayInputStream(backupOutputStream.toByteArray()))) {
                game.state = kryo.readObject(input, (Class<S>) game.state.getClass());
            }
            throw t;
        }
        int[] randomHistory = random.getHistory();

        for (Connection other : server.getConnections()) {
            if (game.hasConnection(other.getID())) {
                other.sendTCP(new GameAction(game.id, action, randomHistory));
            }
        }
    }

    public void removeSpectator(int connectionId) {
        for (ServerGameData<S, A> game : games.values()) {
            game.removeConnection(connectionId);
        }
    }

    public GameService<S, A> getService() {
        return gameService;
    }

    public ServerGameData<S, A> getGame(UUID gameId) {
        return games.get(gameId);
    }

    public List<ServerGameData<S, A>> getGames() {
        return new ArrayList<>(games.values());
    }

    public void addConnectionListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(Listener listener) {
        listeners.remove(listener);
    }

    public Server getKryoServer() {
        return server;
    }

    public void stop() {
        server.stop();
    }

}
