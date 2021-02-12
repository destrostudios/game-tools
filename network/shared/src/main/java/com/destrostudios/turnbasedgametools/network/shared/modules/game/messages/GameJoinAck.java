package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameJoinAck {

    public UUID gameId;
    public Object state;

    GameJoinAck() {
    }

    public GameJoinAck(UUID gameId, Object state) {
        this.gameId = gameId;
        this.state = state;
    }
}
