package com.destrostudios.gametools.network.server.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.GameModule;
import com.destrostudios.gametools.network.shared.modules.game.GameService;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameActionRequest;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoin;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoinRequest;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Connection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServerModule<S, A> extends GameModule<S, A> {

    private static final Logger LOG = LoggerFactory.getLogger(GameServerModule.class);

    private final Map<UUID, ServerGameData<S>> games = new HashMap<>();
    private final Supplier<Connection[]> connectionsSupply;

    public GameServerModule(GameService<S, A> gameService, Supplier<Connection[]> connectionsSupply) {
        super(gameService);
        this.connectionsSupply = connectionsSupply;
    }

    @Override
    public void disconnected(Connection connection) {
        removeSpectator(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof GameJoinRequest message) {
            join(connection, message.gameId());
        } else if (object instanceof GameActionRequest message) {
            applyAction(message.game(), (A) message.action());
        }
    }

    public void registerGame(ServerGameData<S> game) {
        games.put(game.id, game);
    }

    public void unregisterGame(UUID gameId) {
        games.remove(gameId);
    }

    public void join(Connection connection, UUID gameId) {
        ServerGameData<S> game = games.get(gameId);
        game.addConnection(connection.getID());
        connection.sendTCP(new GameJoin(game.id, game.state));
    }

    public void applyAction(UUID gameId, A action) {
        ServerGameData<S> game = games.get(gameId);
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

        for (Connection other : connectionsSupply.get()) {
            if (game.hasConnection(other.getID())) {
                other.sendTCP(new GameAction(game.id, action, randomHistory));
            }
        }
    }

    public void removeSpectator(int connectionId) {
        for (ServerGameData<S> game : games.values()) {
            game.removeConnection(connectionId);
        }
    }

    public ServerGameData<S> getGame(UUID gameId) {
        return games.get(gameId);
    }

    public List<ServerGameData<S>> getGames() {
        return List.copyOf(games.values());
    }

}
