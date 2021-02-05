package com.destrostudios.turnbasedgametools.network.client;

import com.destrostudios.turnbasedgametools.network.shared.GameService;
import com.destrostudios.turnbasedgametools.network.shared.KryoUtil;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamesClient {
    private final Client client;
    private final Map<Class<?>, GameService<?, ?>> gameServices = new HashMap<>();
    private final Map<UUID, ClientGameData> games = new ConcurrentHashMap<>();

    public GamesClient(String host, int port, int timeout, GameService<?, ?>... gameServices) throws IOException {
        client = new Client();
        KryoUtil.init(client.getKryo());
        for (GameService<?, ?> gameService : gameServices) {
            this.gameServices.put(gameService.getStateClass(), gameService);
            gameService.initialize(client.getKryo());
        }
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {

            }

            @Override
            public void disconnected(Connection connection) {

            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof GameSpectateAck) {
                    GameSpectateAck message = (GameSpectateAck) object;
                    games.put(message.gameId, new ClientGameData(message.gameId, message.state));
                } else if (object instanceof GameAction) {
                    GameAction message = (GameAction) object;
                    ClientGameData game = games.get(message.gameId);
                    GameService service = GamesClient.this.gameServices.get(game.state.getClass());
                    service.applyAction(game.state, message.action, new SlaveRandom(message.randomHistory));
                }
            }
        });

        client.start();
        client.connect(timeout, host, port);
    }

    public void startNewGame(Class<?> gameType) {
        if (!gameServices.containsKey(gameType)) {
            throw new AssertionError(gameType);
        }
        client.sendTCP(new GameStartRequest(gameType));
    }

    public void sendAction(UUID gameId, Object action) {
        client.sendTCP(new GameActionRequest(gameId, action));
    }

    public void spectate(UUID gameId) {
        client.sendTCP(new GameSpectateRequest(gameId));
    }


    public Collection<ClientGameData> getGames() {
        return games.values();
    }

    public <S> GameService<S, ?> getService(S state) {
        return (GameService<S, ?>) gameServices.get(state.getClass());
    }

    public void stop() {
        client.stop();
    }
}
