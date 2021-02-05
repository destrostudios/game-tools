package com.destrostudios.turnbasedgametools.network.shared.messages;

import java.util.UUID;

public class GameSpectateAck {

    public UUID gameId;
    public Object state;

    GameSpectateAck() {
    }

    public GameSpectateAck(UUID gameId, Object state) {
        this.gameId = gameId;
        this.state = state;
    }
}
