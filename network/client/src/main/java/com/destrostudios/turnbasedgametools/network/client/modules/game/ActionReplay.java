package com.destrostudios.turnbasedgametools.network.client.modules.game;

class ActionReplay<A> {
    public final A action;
    public final int[] randomHistory;

    public ActionReplay(A action, int[] randomHistory) {
        this.action = action;
        this.randomHistory = randomHistory;
    }
}
