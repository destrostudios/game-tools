package com.destrostudios.turnbasedgametools.network.server.modules.game;

import com.destrostudios.turnbasedgametools.network.shared.modules.game.NetworkRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MasterRandom implements NetworkRandom {

    private final Random source;
    private final List<Integer> randomHistory = new ArrayList<>();

    public MasterRandom(Random source) {
        this.source = source;
    }

    public int[] getHistory() {
        return randomHistory.stream().mapToInt(x -> x).toArray();
    }

    @Override
    public int nextInt(int maxExclusive) {
        int value = source.nextInt(maxExclusive);
        randomHistory.add(value);
        return value;
    }
}
