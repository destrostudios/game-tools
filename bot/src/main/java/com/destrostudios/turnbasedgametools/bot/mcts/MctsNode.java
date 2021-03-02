package com.destrostudios.turnbasedgametools.bot.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class MctsNode<H> {

    private static final Object[] EMPTY = new Object[0];

    private float visits = 0;
    private final float[] scores;
    private Object[] childs = EMPTY;

    public MctsNode(int playerCount) {
        this.scores = new float[playerCount];
    }

    public synchronized float visits() {
        return visits;
    }

    public synchronized float[] getScores() {
        return Arrays.copyOf(scores, scores.length);
    }

    public synchronized float score(int teamIndex) {
        return scores[teamIndex];
    }

    public synchronized void updateScores(float[] teamScores) {
        for (int i = 0; i < scores.length; i++) {
            scores[i] += teamScores[i];
        }
        visits++;
    }

    public synchronized MctsNode<H> getChild(H move) {
        return Objects.requireNonNull(getChildOrDefault(move, null));
    }

    public synchronized MctsNode<H> getChildOrDefault(H move, MctsNode<H> defaultValue) {
        for (int i = 0; i < childs.length; i += 2) {
            if (childs[i].equals(move)) {
                return (MctsNode<H>) childs[i + 1];
            }
        }
        return defaultValue;
    }

    public synchronized void addChild(H move, MctsNode node) {
        int index = childs.length;
        childs = Arrays.copyOf(childs, childs.length + 2);
        childs[index] = move;
        childs[index + 1] = node;
    }

    public synchronized List<H> getMoves() {
        List<H> result = new ArrayList<>();
        for (int i = 0; i < childs.length; i += 2) {
            result.add((H) childs[i]);
        }
        return result;
    }

    public synchronized List<MctsNode<H>> getChilds() {
        List<MctsNode<H>> result = new ArrayList<>();
        for (int i = 1; i < childs.length; i += 2) {
            result.add((MctsNode<H>) childs[i]);
        }
        return result;
    }
}
