package com.destrostudios.turnbasedgametools.bot;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface BotGameState<A, T> {

    BotActionReplay<A> applyAction(A action);

    void replayAction(BotActionReplay<A> action);

    T activeTeam();

    List<A> generateActions(T team);

    List<BotActionReplay<A>> getHistory();

    void writeTo(OutputStream out);

    void readFrom(InputStream in);

    Map<T, Integer> gameResultRanking(); // returns null for running games

    default boolean isGameOver() {
        return gameResultRanking() != null;
    }

    List<T> getTeams();// immutable
}
