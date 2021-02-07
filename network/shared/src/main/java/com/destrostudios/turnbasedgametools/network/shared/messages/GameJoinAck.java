package com.destrostudios.turnbasedgametools.network.shared.messages;

import java.util.UUID;

public class GameJoinAck {

    public UUID gameId;
    public Object state;
    public Object[] tags;

    GameJoinAck() {
    }

    public GameJoinAck(UUID gameId, Object state, Object[] tags) {
        this.gameId = gameId;
        this.state = state;
        this.tags = tags;
    }
}
