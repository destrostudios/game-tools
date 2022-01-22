package com.destrostudios.gametools.bot;

import java.util.Arrays;

public class BotActionReplay<A> {
    public final A action;
    public final int[] randomHistory;

    public BotActionReplay(A action, int[] randomHistory) {
        this.action = action;
        this.randomHistory = randomHistory;
    }

    @Override
    public String toString() {
        return "BotActionReplay{" +
                "action=" + action +
                ", randomHistory=" + Arrays.toString(randomHistory) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BotActionReplay)) {
            return false;
        }
        BotActionReplay<?> that = (BotActionReplay<?>) o;
        return action.equals(that.action) && Arrays.equals(randomHistory, that.randomHistory);
    }

    @Override
    public int hashCode() {
        return 31 * action.hashCode() + Arrays.hashCode(randomHistory);
    }
}
