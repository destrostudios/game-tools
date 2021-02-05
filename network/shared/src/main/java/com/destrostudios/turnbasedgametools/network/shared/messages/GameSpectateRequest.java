package com.destrostudios.turnbasedgametools.network.shared.messages;

import java.util.UUID;

public class GameSpectateRequest {

    public UUID gameId;

    GameSpectateRequest() {
    }

    public GameSpectateRequest(UUID gameId) {
        this.gameId = gameId;
    }
}
