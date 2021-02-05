package com.destrostudios.turnbasedgametools.network.shared;

import com.destrostudios.turnbasedgametools.network.shared.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateAck;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameSpectateRequest;
import com.destrostudios.turnbasedgametools.network.shared.messages.GameStartRequest;
import com.esotericsoftware.kryo.Kryo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class KryoUtil {

    public static int PORT = 52156;

    public static void init(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(int[].class);
        kryo.register(HashMap.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(ArrayList.class);
        kryo.register(Class.class);

        kryo.register(GameAction.class);
        kryo.register(GameActionRequest.class);
        kryo.register(GameSpectateAck.class);
        kryo.register(GameStartRequest.class);
        kryo.register(GameSpectateRequest.class);

    }
}
