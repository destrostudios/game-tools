package com.destrostudios.turnbasedgametools.bot.mcts;

import com.destrostudios.turnbasedgametools.bot.BotActionReplay;
import com.destrostudios.turnbasedgametools.bot.BotGameService;
import com.destrostudios.turnbasedgametools.bot.BotGameState;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MctsBotWorker<S extends BotGameState<A, T>, A, T> {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBotWorker.class);

    private static final float EPSILON = 1e-6f;
    private final BotGameService<S, A, T> service;
    private final float uctConstant;
    private final float firstPlayUrgency;
    private final Function<S, float[]> evaluation;
    private final Random random;
    private final float raveMultiplier;
    private final byte[] sourceGame;
    private final int teamCount;
    private final MctsNode<BotActionReplay<A>> rootNode;
    private final MctsRaveScores raveScores;

    private S simulationGame;// use method variables/parameters instead

    public MctsBotWorker(BotGameService<S, A, T> service, byte[] sourceGame, MctsBotSettings<S, A> settings, int teamCount, MctsNode<BotActionReplay<A>> rootNode, MctsRaveScores raveScores) {
        this.service = service;
        this.sourceGame = sourceGame;
        this.random = settings.random;
        this.uctConstant = settings.uctConstant;
        this.firstPlayUrgency = settings.firstPlayUrgency;
        this.evaluation = settings.evaluation;
        this.raveMultiplier = settings.raveMultiplier;
        this.teamCount = teamCount;
        this.rootNode = rootNode;
        this.raveScores = raveScores;
    }

    public void run(BooleanSupplier isActive) {
        LOG.debug("worker started.");
        int iterations = 0;
        while (isActive.getAsBoolean()) {
            ByteArrayInputStream in = new ByteArrayInputStream(sourceGame);
            simulationGame = service.deserialize(in);
//            // randomize opponents hand cards
//            // TODO: use a priori knowledge for better approximation of real hand cards
//            // TODO: the simulated opponent also only 'knows' our hand...
//            simulationGame.randomizeHiddenInformation(random, botPlayerIndex);
            iteration(rootNode, raveScores);
            iterations++;
            if (Thread.interrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
        }
        LOG.debug("worker finished after {} iterations.", iterations);
    }

    private void iteration(MctsNode<BotActionReplay<A>> rootNode, MctsRaveScores raveScores) {
        Deque<MctsNode<BotActionReplay<A>>> nodePath = new LinkedList<>();
        Deque<BotActionReplay<A>> movePath = new LinkedList<>();
        nodePath.add(rootNode);
        A selectedMove = select(nodePath, movePath, raveScores);

        if (selectedMove != null) {
            MctsNode<BotActionReplay<A>> child = new MctsNode<>(teamCount);
            BotActionReplay<A> action = simulationGame.applyAction(selectedMove);
            nodePath.getLast().addChild(action, child);
            nodePath.add(child);
            movePath.add(action);
        }

        float[] result = evaluation.apply(simulationGame);
        for (MctsNode<BotActionReplay<A>> node : nodePath) {
            node.updateScores(result);
        }
        for (BotActionReplay<A> move : movePath) {
            raveScores.updateScores(move, result);
        }
        float[] avgWeights = Arrays.copyOf(result, result.length);
        for (int i = 0; i < avgWeights.length; i++) {
            avgWeights[i] *= movePath.size();
        }
        raveScores.getDefaultScore().updateScores(avgWeights);
    }

    private A select(Deque<MctsNode<BotActionReplay<A>>> nodePath, Deque<BotActionReplay<A>> movePath, MctsRaveScores raveScores) {
        MctsNode<BotActionReplay<A>> node = nodePath.getLast();
        A selectedMove = uctSelect(node, raveScores);
        while (true) {
            List<MctsNode<BotActionReplay<A>>> childs = getChilds(node, selectedMove);
            if (childs.isEmpty()) {
                break;
            }
            BotActionReplay<A> action = simulationGame.applyAction(selectedMove);
            node = getChild(node, action);
            nodePath.add(node);
            movePath.add(action);
            if (simulationGame.isGameOver()) {
                return null;
            }
            selectedMove = uctSelect(nodePath.getLast(), raveScores);

        }
//        while ((node = getChild(node, selectedMove)) != null) {
//            nodePath.add(node);
//            movePath.add(selectedMove);
//            BotActionReplay<A> action = simulationGame.applyAction(selectedMove);
//            if (simulationGame.isGameOver()) {
//                return null;
//            }
//            selectedMove = uctSelect(nodePath.getLast(), raveScores);
//        }
        return selectedMove;
    }

    private A uctSelect(MctsNode<BotActionReplay<A>> node, MctsRaveScores raveScores) {
        T team = simulationGame.activeTeam();
//        int activePlayerIndex = -1;
//        if (simulationGame.isPlayerIndexActive(botPlayerIndex)) {
//            activePlayerIndex = botPlayerIndex;
//        } else {
//            for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {
//                if (simulationGame.isPlayerIndexActive(playerIndex)) {
//                    activePlayerIndex = playerIndex;
//                    break;
//                }
//            }
//        }
//        assert activePlayerIndex >= 0 : "there is no active player";
//        //TODO: generate moves for all active players instead, WARNING: might need special handling in root since we only want own moves there
//        List<A> moves = simulationGame.generateMoves(activePlayerIndex);
        List<A> moves = simulationGame.generateActions(team);

        if (moves.size() == 1) {
            return moves.get(0);
        }
        List<A> bestMoves = new ArrayList<>();
        float bestValue = Float.NEGATIVE_INFINITY;
        for (A move : moves) {
            int moveTeamIndex = simulationGame.getTeams().indexOf(team);
            float score;
            List<MctsNode<BotActionReplay<A>>> childs = getChilds(node, move);
            if (childs.isEmpty()) {
                score = firstPlayUrgency;
            } else {
                float childsVisits = 0;
                float childsScore = 0;
                for (MctsNode<BotActionReplay<A>> child : childs) {
                    childsVisits += child.visits();
                    childsScore += child.score(moveTeamIndex);
                }
                score = calcUtc(node.visits(), childsVisits, childsScore);
            }

            MctsRaveScore raveScore = raveScores.get(move);
            if (raveScore == null) {
                raveScore = raveScores.get(null);
            }
            float raveValue = raveScore.getScore(moveTeamIndex) / (node.visits() + 1);
            score += raveMultiplier * raveValue;

            if (score > bestValue) {
                bestMoves.clear();
                bestValue = score;
            }
            if (score == bestValue) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.size() == 1) {
            return bestMoves.get(0);
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private float calcUtc(float parentTotal, float childTotal, float childScore) {
        float exploitation = childScore / (childTotal + EPSILON);
        float exploration = (float) (Math.sqrt(uctConstant * Math.log(parentTotal + 1) / (childTotal + EPSILON)));
        return exploitation + exploration;
    }

    private MctsNode<BotActionReplay<A>> getChild(MctsNode<BotActionReplay<A>> node, BotActionReplay<A> move) {
        return node.getChildOrDefault(move, null);
    }

    private List<MctsNode<BotActionReplay<A>>> getChilds(MctsNode<BotActionReplay<A>> node, A move) {
        List<BotActionReplay<A>> moves = node.getMoves();
        List<MctsNode<BotActionReplay<A>>> result = new ArrayList<>();
        for (BotActionReplay<A> actionReplay : moves) {
            if (actionReplay.action.equals(move)) {
                result.add(node.getChild(actionReplay));
            }
        }
        return result;
    }
}
