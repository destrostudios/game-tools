package com.destrostudios.gametools.network.client.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.GameStartModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameStartRequest;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import java.util.function.Consumer;

public class GameStartClientModule<P> extends GameStartModule<P> {

    private final Connection connection;

    public GameStartClientModule(Consumer<Kryo> registerParams, Connection connection) {
        super(registerParams);
        this.connection = connection;
    }

    public void startNewGame(P params) {
        connection.sendTCP(new GameStartRequest(params));
    }
}
