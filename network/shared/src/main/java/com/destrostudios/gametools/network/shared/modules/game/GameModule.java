package com.destrostudios.gametools.network.shared.modules.game;

import com.destrostudios.gametools.network.shared.modules.NetworkModule;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameActionRequest;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoin;
import com.destrostudios.gametools.network.shared.modules.game.messages.GameJoinRequest;
import com.destrostudios.gametools.network.shared.serializers.RecordSerializer;
import com.destrostudios.gametools.network.shared.serializers.UuidSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import java.util.UUID;

public abstract class GameModule<S, A> extends NetworkModule {

    protected final GameService<S, A> gameService;

    public GameModule(GameService<S, A> gameService) {
        this.gameService = gameService;
    }

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());

        kryo.register(int[].class, new DefaultArraySerializers.IntArraySerializer());

        kryo.register(GameAction.class, new RecordSerializer<>());
        kryo.register(GameActionRequest.class, new RecordSerializer<>());
        kryo.register(GameJoin.class, new RecordSerializer<>());
        kryo.register(GameJoinRequest.class, new RecordSerializer<>());

        gameService.initialize(kryo);
    }

    public GameService<S, A> getGameService() {
        return gameService;
    }
}
