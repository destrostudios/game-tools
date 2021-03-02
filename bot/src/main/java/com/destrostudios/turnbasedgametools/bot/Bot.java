package com.destrostudios.turnbasedgametools.bot;

import java.util.List;

public interface Bot<A, T> {

    List<A> sortedActions(T team);
}
