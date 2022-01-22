package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public class GameActionRequest {

    public UUID game;
    public Object action;

    GameActionRequest() {
    }

    public GameActionRequest(UUID game, Object action) {
        this.game = game;
        this.action = action;
    }
}
