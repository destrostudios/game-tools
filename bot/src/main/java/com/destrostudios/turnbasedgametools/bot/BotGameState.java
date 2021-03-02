package com.destrostudios.turnbasedgametools.bot;

import java.util.List;

public interface BotGameState<A, T> {

    BotActionReplay<A> applyAction(A action);

    T activeTeam();

    List<A> generateActions(T team);

    List<BotActionReplay<A>> getHistory();

    boolean isGameOver();

    List<T> getTeams();// immutable
}
