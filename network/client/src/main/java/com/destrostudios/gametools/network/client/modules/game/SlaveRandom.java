package com.destrostudios.gametools.network.client.modules.game;


import com.destrostudios.gametools.network.shared.modules.game.NetworkRandom;

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
