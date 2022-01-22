package com.destrostudios.gametools.bot.mcts;

import java.util.Random;
import java.util.function.Function;

public class MctsBotSettings<S, A> {

    public boolean verbose = false;
    public int maxThreads = 1;
    public int strength = 10_000;
    public float uctConstant = 2;
    public float raveMultiplier = 1;
    public float firstPlayUrgency = 10;
    public Random random = new Random();
    public Function<S, float[]> evaluation;// = new RolloutToEvaluation<>(new Random(), 10, new SimpleTeamEvaluation<A, S>()::evaluate)::evaluate;
    public TerminationType termination = TerminationType.NODE_COUNT;
}
