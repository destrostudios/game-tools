package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameJoin {

    public UUID gameId;
    public int version;
    public Object state;

    GameJoin() {
    }

    public GameJoin(UUID gameId, int version, Object state) {
        this.gameId = gameId;
        this.version = version;
        this.state = state;
    }
}
