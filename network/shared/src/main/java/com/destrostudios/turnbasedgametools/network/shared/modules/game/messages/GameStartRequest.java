package com.destrostudios.turnbasedgametools.network.shared.modules.game.messages;

public class GameStartRequest<P> {

    public P params;

    GameStartRequest() {
    }

    public GameStartRequest(P params) {
        this.params = params;
    }
}
