package com.destrostudios.turnbasedgametools.network.shared.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.UuidSerializer;
import com.destrostudios.turnbasedgametools.network.shared.modules.NetworkModule;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameAction;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameActionRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameJoinAck;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameJoinRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.GameStartRequest;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.ListGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.SubscribeGamesList;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnlistGame;
import com.destrostudios.turnbasedgametools.network.shared.modules.game.messages.UnsubscribeGamesList;
import com.esotericsoftware.kryo.Kryo;
import java.util.UUID;

public abstract class GameModule<S, A> extends NetworkModule {

    protected final GameService<S, A> gameService;

    public GameModule(GameService<S, A> gameService) {
        this.gameService = gameService;
    }

    @Override
    public void initialize(Kryo kryo) {
        kryo.register(UUID.class, new UuidSerializer());
        kryo.register(int[].class);
        kryo.register(Object[].class);

        kryo.register(GameAction.class);
        kryo.register(GameActionRequest.class);
        kryo.register(GameJoinAck.class);
        kryo.register(GameStartRequest.class);
        kryo.register(GameJoinRequest.class);

        kryo.register(SubscribeGamesList.class);
        kryo.register(UnsubscribeGamesList.class);
        kryo.register(UnlistGame.class);
        kryo.register(ListGame.class);

        gameService.initialize(kryo);
    }

    public GameService<S, A> getGameService() {
        return gameService;
    }
}
