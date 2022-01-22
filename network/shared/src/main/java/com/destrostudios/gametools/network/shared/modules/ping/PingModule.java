package com.destrostudios.gametools.network.shared.modules.ping;

import com.destrostudios.gametools.network.shared.modules.ping.messages.Pong;
import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.ping.messages.Ping;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;

public class PingModule extends NetworkModule {

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(Ping.class);
        kryo.register(Pong.class);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof Ping) {
            connection.sendTCP(new Pong());
        }
    }
}
