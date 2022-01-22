package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameJoinRequest {

    public UUID gameId;

    GameJoinRequest() {
    }

    public GameJoinRequest(UUID gameId) {
        this.gameId = gameId;
    }
}
