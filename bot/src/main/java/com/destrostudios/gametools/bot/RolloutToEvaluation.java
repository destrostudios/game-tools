package com.destrostudios.gametools.bot;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class RolloutToEvaluation<S extends BotGameState<A, T>, A, T> {

    private final Random random;
    private final int rolloutMoves;
    private final Function<S, float[]> evaluation;

    public RolloutToEvaluation(Random random, int rolloutMoves, Function<S, float[]> evaluation) {
        this.random = random;
        this.rolloutMoves = rolloutMoves;
        this.evaluation = evaluation;
    }

    public float[] evaluate(S game) {
        for (int i = 0; i < rolloutMoves && !game.isGameOver(); i++) {
            List<A> moves = game.generateActions(game.activeTeam());
            A move = moves.get(random.nextInt(moves.size()));
            game.applyAction(move);
        }
        return evaluation.apply(game);
    }
}