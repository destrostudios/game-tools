package com.destrostudios.turnbasedgametools.network.shared.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameStartRequest;
import com.esotericsoftware.kryo.Kryo;
import java.util.function.Consumer;

public abstract class GameStartModule<P> extends NetworkModule {

    private final Consumer<Kryo> registerParams;

    protected GameStartModule(Consumer<Kryo> registerParams) {
        this.registerParams = registerParams;
    }

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(GameStartRequest.class);

        registerParams.accept(kryo); // registers P
    }
}
