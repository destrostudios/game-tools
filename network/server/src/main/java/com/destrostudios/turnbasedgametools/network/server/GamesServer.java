package com.destrostudios.turnbasedgametools.network.server;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.KryoUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamesServer {
    private final Server server;
    private final Map<Class<?>, GameService<?, ?>> gameServices = new HashMap<>();
    private final Map<UUID, ServerGameData> games = new ConcurrentHashMap<>();

    public GamesServer(int port, GameService<?, ?>... gameServices) throws IOException {
        server = new Server();
        KryoUtil.init(server.getKryo());
        for (GameService<?, ?> gameService : gameServices) {
            this.gameServices.put(gameService.getStateClass(), gameService);
            gameService.initialize(server.getKryo());
        }
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
            }

            @Override
            public void disconnected(Connection connection) {
                removeSpectator(connection.getID());
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof GameSpectateRequest) {
                    GameSpectateRequest message = (GameSpectateRequest) object;
                    ServerGameData game = getGame(message.gameId);
                    game.addSpectator(connection.getID());
                    connection.sendTCP(new GameSpectateAck(game.id, game.state));
                } else if (object instanceof GameActionRequest) {
                    GameActionRequest message = (GameActionRequest) object;
                    ServerGameData game = getGame(message.game);
                    GameService service = getService(game.state);
                    MasterRandom random = new MasterRandom(game.random);
                    service.applyAction(game.state, message.action, random);
                    int[] randomHistory = random.getHistory();

                    for (Connection other : server.getConnections()) {
                        if (game.hasSpectator(other.getID())) {
                            other.sendTCP(new GameAction(game.id, message.action, randomHistory));
                        }
                    }
                } else if (object instanceof GameStartRequest) {
                    GameStartRequest message = (GameStartRequest) object;
                    GameService service = GamesServer.this.gameServices.get(message.gameType);
                    UUID id = UUID.randomUUID();
                    Object state = service.startNewGame();
                    ServerGameData game = new ServerGameData(id, state, new SecureRandom());
                    games.put(id, game);
                    game.addSpectator(connection.getID());
                    connection.sendTCP(new GameSpectateAck(id, state));
                }
            }
        });
        server.start();
        server.bind(port);
    }

    private UUID startGame(Object state, Random random) {
        UUID id = UUID.randomUUID();
        games.put(id, new ServerGameData(id, state, random));
        return id;
    }

    public ServerGameData getGame(UUID gameId) {
        return games.get(gameId);
    }

    public void removeSpectator(int connectionId) {
        for (ServerGameData game : games.values()) {
            game.removeSpectator(connectionId);
        }
    }

    public <S> GameService<S, ?> getService(S state) {
        return (GameService<S, ?>) gameServices.get(state.getClass());
    }

    public void stop() {
        server.stop();
    }

}
