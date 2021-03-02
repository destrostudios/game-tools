package com.destrostudios.turnbasedgametools.bot;

import java.io.InputStream;
import java.io.OutputStream;

public interface BotGameService<S extends BotGameState<A, T>, A, T> {

//    H applyAction(S state, A action);
//
//    void replayAction(S state, H action);
//
//    List<A> generateActions(S state);
//
//    List<H> getHistory(S state);

    void serialize(S state, OutputStream out);

    S deserialize(InputStream in);

//    int[] gameResultRanking(S state); // returns null for running games
//
//    A getAction(H move);
//
//    default boolean isGameOver(S state) {
//        return gameResultRanking(state) != null;
//    }
}
