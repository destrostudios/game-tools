package com.destrostudios.turnbasedgametools.network.shared.messages;

import java.util.Set;
import java.util.UUID;

public class GameJoinAck {

    public UUID gameId;
    public Object state;
    public Set<Object> tags;

    GameJoinAck() {
    }

    public GameJoinAck(UUID gameId, Object state, Set<Object> tags) {
        this.gameId = gameId;
        this.state = state;
        this.tags = tags;
    }
}
