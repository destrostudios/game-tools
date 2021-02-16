package com.destrostudios.turnbasedgametools.network.client.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.game.LobbyModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnsubscribeGamesList;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LobbyClientModule<P> extends LobbyModule<P> {

    private final Connection connection;
    private final Map<UUID, P> listedGames = new ConcurrentHashMap<>();

    public LobbyClientModule(Consumer<Kryo> registerParams, Connection connection) {
        super(registerParams);
        this.connection = connection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ListGame) {
            ListGame<P> message = (ListGame<P>) object;
            listedGames.put(message.gameId, message.params);
        } else if (object instanceof UnlistGame) {
            UnlistGame message = (UnlistGame) object;
            listedGames.remove(message.gameId);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        listedGames.clear();
    }

    public void subscribeToGamesList() {
        connection.sendTCP(new SubscribeGamesList());
    }

    public void unsubscribeFromGamesList() {
        connection.sendTCP(new UnsubscribeGamesList());
    }

    public Map<UUID, P> getListedGames() {
        return Map.copyOf(listedGames);
    }
}
