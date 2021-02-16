package com.destrostudios.turnbasedgametools.network.shared.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.UuidSerializer;
import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnsubscribeGamesList;
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

        kryo.register(SubscribeGamesList.class);
        kryo.register(UnsubscribeGamesList.class);
        kryo.register(UnlistGame.class);
        kryo.register(ListGame.class);

        registerParams.accept(kryo); // registers P
    }

}
