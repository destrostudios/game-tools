package com.destrostudios.turnbasedgametools.network.server;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.NetworkUtil;
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
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamesServer<S, A> {
    private final Server server;
    private final GameService<S, A> gameService;
    private final Map<UUID, ServerGameData<S, A>> games = new ConcurrentHashMap<>();

    public GamesServer(int port, GameService<S, A> service) throws IOException {
        gameService = service;
        server = new Server();
        NetworkUtil.initialize(server.getKryo());
        gameService.initialize(server.getKryo());
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
                    ServerGameData<S, A> game = games.get(message.gameId);
                    game.addSpectator(connection.getID());
                    connection.sendTCP(new GameSpectateAck(game.id, game.state));
                } else if (object instanceof GameActionRequest) {
                    GameActionRequest message = (GameActionRequest) object;
                    ServerGameData<S, A> game = games.get(message.game);
                    MasterRandom random = new MasterRandom(game.random);
                    game.state = gameService.applyAction(game.state, (A) message.action, random);
                    int[] randomHistory = random.getHistory();

                    for (Connection other : server.getConnections()) {
                        if (game.hasSpectator(other.getID())) {
                            other.sendTCP(new GameAction(game.id, message.action, randomHistory));
                        }
                    }
                } else if (object instanceof GameStartRequest) {
                    UUID id = UUID.randomUUID();
                    S state = gameService.startNewGame();
                    ServerGameData<S, A> game = new ServerGameData<>(id, state, new SecureRandom());
                    games.put(id, game);
                    game.addSpectator(connection.getID());
                    connection.sendTCP(new GameSpectateAck(id, state));
                }
            }
        });
        server.start();
        server.bind(port);
    }

    private UUID startGame(S state, Random random) {
        UUID id = UUID.randomUUID();
        games.put(id, new ServerGameData<>(id, state, random));
        return id;
    }

    public void removeSpectator(int connectionId) {
        for (ServerGameData<S, A> game : games.values()) {
            game.removeSpectator(connectionId);
        }
    }

    public GameService<S, A> getService() {
        return gameService;
    }

    public void stop() {
        server.stop();
    }

}
