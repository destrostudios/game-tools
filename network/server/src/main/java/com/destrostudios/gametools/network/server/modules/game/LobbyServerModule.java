package com.destrostudios.gametools.network.server.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.LobbyModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.gametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.gametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.gametools.network.shared.modules.game.messages.UnsubscribeGamesList;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LobbyServerModule<P> extends LobbyModule {
    private final Set<Integer> gamesListSubscribers = new CopyOnWriteArraySet<>();
    private final Map<UUID, P> games = new HashMap<>();
    private final Supplier<Connection[]> connectionsSupply;

    public LobbyServerModule(Consumer<Kryo> registerParams, Supplier<Connection[]> connectionsSupply) {
        super(registerParams);
        this.connectionsSupply = connectionsSupply;
    }

    @Override
    public void disconnected(Connection connection) {
        gamesListSubscribers.remove(connection.getID());
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof SubscribeGamesList) {
            subscribeToGamesList(connection);
        } else if (object instanceof UnsubscribeGamesList) {
            unsubscribeFromMovesList(connection);
        }
    }

    public void listGame(UUID gameId, P game) {
        games.put(gameId, game);
        for (Connection connection : connectionsSupply.get()) {
            if (gamesListSubscribers.contains(connection.getID())) {
                connection.sendTCP(new ListGame<>(gameId, game));
            }
        }
    }

    public void unlistGame(UUID gameId) {
        games.remove(gameId);
        for (Connection connection : connectionsSupply.get()) {
            if (gamesListSubscribers.contains(connection.getID())) {
                connection.sendTCP(new UnlistGame(gameId));
            }
        }
    }

    public void subscribeToGamesList(Connection connection) {
        gamesListSubscribers.add(connection.getID());
        for (Map.Entry<UUID, P> entry : games.entrySet()) {
            connection.sendTCP(new ListGame(entry.getKey(), entry.getValue()));
        }
    }

    public void unsubscribeFromMovesList(Connection connection) {
        gamesListSubscribers.remove(connection.getID());
        for (UUID gameId : games.keySet()) {
            connection.sendTCP(new UnlistGame(gameId));
        }
    }

    public Map<UUID, P> getGames() {
        return Map.copyOf(games);
    }
}
