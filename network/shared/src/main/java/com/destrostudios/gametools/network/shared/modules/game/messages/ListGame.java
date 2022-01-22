package com.destrostudios.gametools.network.shared.modules.game.messages;

import java.util.UUID;

public class ListGame<P> {

    public UUID gameId;
    public P params;

    ListGame() {
    }

    public ListGame(UUID gameId, P params) {
        this.gameId = gameId;
        this.params = params;
    }
}
