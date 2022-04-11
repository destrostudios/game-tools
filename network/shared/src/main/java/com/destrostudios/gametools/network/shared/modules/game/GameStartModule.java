package com.destrostudios.gametools.network.shared.modules.game;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameStartRequest;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.esotericsoftware.kryo.Kryo;
import java.util.function.Consumer;

public abstract class GameStartModule<P> extends NetworkModule {

    private final Consumer<Kryo> registerParams;

    protected GameStartModule(Consumer<Kryo> registerParams) {
        this.registerParams = registerParams;
    }

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(GameStartRequest.class, new RecordSerializer<>());

        registerParams.accept(kryo); // registers P
    }
}
