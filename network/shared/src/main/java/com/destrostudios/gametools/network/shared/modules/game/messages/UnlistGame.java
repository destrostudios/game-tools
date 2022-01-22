package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public class UnlistGame {

    public UUID gameId;

    UnlistGame() {
    }

    public UnlistGame(UUID gameId) {
        this.gameId = gameId;
    }
}
