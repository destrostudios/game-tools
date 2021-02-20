package com.destrostudios.turnbasedgametools.network.client.modules.game;

import java.util.Arrays;

class ActionReplay<A> {
    public final A action;
    public final int version;
    public final int[] randomHistory;

    public ActionReplay(A action, int version, int[] randomHistory) {
        this.action = action;
        this.version = version;
        this.randomHistory = randomHistory;
    }

    @Override
    public String toString() {
        return "ActionReplay{" +
                "action=" + action +
                ", version=" + version +
                ", randomHistory=" + Arrays.toString(randomHistory) +
                '}';
    }
}
