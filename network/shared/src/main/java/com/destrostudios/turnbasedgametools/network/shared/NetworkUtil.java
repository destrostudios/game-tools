package com.destrostudios.turnbasedgametools.network.shared;

import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.Ping;
import com.destrostudios.turnbasedgametools.network.shared.messages.Pong;
import com.esotericsoftware.kryo.Kryo;
import java.util.UUID;

public class NetworkUtil {

    public static int PORT = 52156;

    public static void initialize(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(int[].class);

        kryo.register(GameAction.class);
        kryo.register(GameActionRequest.class);
        kryo.register(GameSpectateAck.class);
        kryo.register(GameStartRequest.class);
        kryo.register(GameSpectateRequest.class);

        kryo.register(Ping.class);
        kryo.register(Pong.class);
    }
}
