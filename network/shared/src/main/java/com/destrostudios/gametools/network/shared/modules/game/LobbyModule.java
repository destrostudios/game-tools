package com.destrostudios.gametools.network.shared.modules.game;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.gametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.gametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.gametools.network.shared.modules.game.messages.UnsubscribeGamesList;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.destrostudios.gametools.network.shared.serializers.UuidSerializer;
import com.esotericsoftware.kryo.Kryo;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class LobbyModule<P> extends NetworkModule {

    private final Consumer<Kryo> registerParams;

    public LobbyModule(Consumer<Kryo> registerParams) {
        this.registerParams = registerParams;
    }

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());

        kryo.register(SubscribeGamesList.class, new RecordSerializer<>());
        kryo.register(UnsubscribeGamesList.class, new RecordSerializer<>());
        kryo.register(UnlistGame.class, new RecordSerializer<>());
        kryo.register(ListGame.class, new RecordSerializer<>());

        registerParams.accept(kryo); // registers P
    }

}
