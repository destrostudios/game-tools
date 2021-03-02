package com.destrostudios.turnbasedgametools.bot.mcts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MctsRaveScores<A> {

    private final Map<A, MctsRaveScore> map = new ConcurrentHashMap<>();
    private final MctsRaveScore defaultScore;
    private final int playerCount;

    public MctsRaveScores(int playerCount) {
        this.playerCount = playerCount;
        this.defaultScore = new MctsRaveScore(playerCount);
    }

    public MctsRaveScore getDefaultScore() {
        return defaultScore;
    }

    public void updateScores(A move, float[] scores) {
        map.computeIfAbsent(move, x -> new MctsRaveScore(playerCount)).updateScores(scores);
    }

    public MctsRaveScore get(A move) {
        return map.getOrDefault(move, defaultScore);
    }

}
