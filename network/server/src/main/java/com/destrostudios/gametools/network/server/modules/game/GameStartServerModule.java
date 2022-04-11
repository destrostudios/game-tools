package com.destrostudios.gametools.network.server.modules.game;

import com.destrostudios.gametools.network.shared.modules.game.GameStartModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameStartRequest;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import java.util.function.Consumer;

public abstract class GameStartServerModule<P> extends GameStartModule<P> {


    public GameStartServerModule(Consumer<Kryo> registerParams) {
        super(registerParams);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof GameStartRequest) {
            GameStartRequest<P> message = (GameStartRequest<P>) object;
            startGameRequest(connection, message.params());
        }
    }

    public abstract void startGameRequest(Connection connection, P params);
}
