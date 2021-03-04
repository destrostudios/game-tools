package com.destrostudios.turnbasedgametools.bot;

import java.util.List;

public interface Bot<S, A, T> {

    List<A> sortedActions(S state, T team);
}
