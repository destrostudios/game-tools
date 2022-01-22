package com.destrostudios.gametools.bot;

import java.util.List;

public interface BotGameState<A, T> {

    BotActionReplay<A> applyAction(A action);

    T activeTeam();

    List<A> generateActions(T team);

    boolean isGameOver();

    List<T> getTeams();// immutable
}
