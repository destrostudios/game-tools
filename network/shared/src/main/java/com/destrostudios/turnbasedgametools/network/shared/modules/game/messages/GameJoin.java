package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameJoin {

    public UUID gameId;
    public Object state;

    GameJoin() {
    }

    public GameJoin(UUID gameId, Object state) {
        this.gameId = gameId;
        this.state = state;
    }
}
