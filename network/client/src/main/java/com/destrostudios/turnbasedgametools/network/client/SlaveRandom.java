package com.destrostudios.turnbasedgametools.network.client;


import com.destrostudios.turnbasedgametools.network.shared.NetworkRandom;

public class SlaveRandom implements NetworkRandom {
    private final int[] history;
    private int pointer = 0;

    public SlaveRandom(int[] history) {
        this.history = history;
    }

    @Override
    public int nextInt(int maxExclusive) {
        return history[pointer++];
    }
}
