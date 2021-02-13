package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

import java.util.UUID;

public class ListGame {
    public UUID gameId;

    ListGame() {
    }

    public ListGame(UUID gameId) {
        this.gameId = gameId;
    }
}
