package com.destrostudios.turnbasedgametools.bot.mcts;

import com.destrostudios.turnbasedgametools.bot.Bot;
import com.destrostudios.turnbasedgametools.bot.BotActionReplay;
import com.destrostudios.turnbasedgametools.bot.BotGameService;
import com.destrostudios.turnbasedgametools.bot.BotGameState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MctsBot<S extends BotGameState<A, T>, A, T, D> implements Bot<A, T> {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBot.class);

    private static final int MILLI_TO_NANO = 1_000_000;

    private final BotGameService<S, A, T, D> gameService;
    private final MctsBotSettings<S, A> settings;
    private final S sourceGame;

    private MctsNode<BotActionReplay<A>> rootNode;
    private int rootNodeHistoryPointer = 0;

    public MctsBot(BotGameService<S, A, T, D> gameService, S sourceGame, MctsBotSettings<S, A> settings) {
        this.gameService = gameService;
        this.settings = settings;
        this.sourceGame = sourceGame;
    }

    @Override
    public List<A> sortedActions(T team) {
        assert !sourceGame.isGameOver();
        if (sourceGame.getHistory() == null) {
            rootNode = null;
        } else {
            while (rootNodeHistoryPointer < sourceGame.getHistory().size()) {
                BotActionReplay<A> move = sourceGame.getHistory().get(rootNodeHistoryPointer);
                if (rootNode != null) {
                    rootNode = getChild(rootNode, move);
                }
                rootNodeHistoryPointer++;
            }
        }
        if (rootNode == null) {
            rootNode = createNode();
        }

        List<A> moves = new ArrayList<>(sourceGame.generateActions(sourceGame.activeTeam()));
        if (moves.size() > 1) {
            MctsRaveScores raveScores = initRaveScores();

            D data = gameService.serialize(sourceGame);

            List<MctsBotWorker> workers = new ArrayList<>();
            for (int i = 0; i < settings.maxThreads; i++) {
                workers.add(new MctsBotWorker(gameService, data, settings, teamCount(), rootNode, raveScores));
            }
            BooleanSupplier isActive;
            int strength = settings.strength;
            switch (settings.termination) {
                case NODE_COUNT:
                    isActive = () -> rootNode.visits() < strength;
                    break;
                case MILLIS_ELAPSED:
                    long startNanos = System.nanoTime();
                    long endNanos = MILLI_TO_NANO * strength + startNanos;
                    isActive = () -> System.nanoTime() < endNanos;
                    break;
                default:
                    throw new AssertionError(settings.termination.name());
            }
            workers.parallelStream().forEach(x -> x.run(isActive));

            MctsNode<BotActionReplay<A>> node = rootNode;
            moves.sort(Comparator.comparingDouble(move -> -visits(node, move)));
//            if (settings.verbose) {
//                LOG.info("Move scores:");
//                for (A move : moves) {
//                    LOG.info("{}: {}", Math.round(visits(node, move)), sourceGame.toMoveString(move));
//                }
//                IntList branching = new IntList();
//                List<MctsNode<A>> nodes = Collections.singletonList(rootNode);
//                while (!nodes.isEmpty()) {
//                    branching.add(nodes.size());
//                    List<MctsNode<A>> nextNodes = new ArrayList<>();
//                    for (MctsNode<A> n : nodes) {
//                        nextNodes.addAll(n.getChilds());
//                    }
//                    nodes = nextNodes;
//                }
//                LOG.info("Tree dimensions: {} - {}", branching.size(), branching.toArray());
//                LOG.info("Expected win-rate: {}%", Math.round(100 * rootNode.score(playerIndex) / rootNode.visits()));
//            }
        }
        return moves;
//        A selected = moves.get(0);
//        float score = visits(rootNode, selected);
//        int identicalMovesCount = 1;
//        while (identicalMovesCount < moves.size() && visits(rootNode, moves.get(identicalMovesCount)) == score) {
//            identicalMovesCount++;
//        }
//        return moves.get(settings.random.nextInt(identicalMovesCount));
    }

    private float visits(MctsNode<BotActionReplay<A>> node, A move) {
        List<BotActionReplay<A>> moves = node.getMoves();
        float sum = 0;
        for (BotActionReplay<A> actionReplay : moves) {
            if (actionReplay.action.equals(move)) {
                sum += node.getChild(actionReplay).visits();
            }
        }
        return sum;
//        MctsNode<A> child = node.getChildOrDefault(move, null);
//        if (child == null) {
//            return 0;
//        }
//        return child.visits();
    }

    private MctsRaveScores initRaveScores() {
        MctsRaveScores raveScores = new MctsRaveScores(teamCount());
        raveScores.getDefaultScore().updateScores(1f / teamCount());
        initRaveScores(raveScores, rootNode);
        return raveScores;
    }

    private void initRaveScores(MctsRaveScores raveScores, MctsNode<BotActionReplay<A>> node) {
        for (BotActionReplay<A> move : node.getMoves()) {
            MctsNode<BotActionReplay<A>> child = node.getChildOrDefault(move, null);
            if (child != null) {
                raveScores.updateScores(move, child.getScores());
                raveScores.getDefaultScore().updateScores(child.getScores());
                initRaveScores(raveScores, child);
            }
        }
    }

    private MctsNode<BotActionReplay<A>> getChild(MctsNode<BotActionReplay<A>> node, BotActionReplay<A> move) {
        return node.getChildOrDefault(move, null);
    }

    private MctsNode<BotActionReplay<A>> createNode() {
        return new MctsNode<>(teamCount());
    }

    private int teamCount() {
        return sourceGame.getTeams().size();
    }

    public S getSourceGame() {
        return sourceGame;
    }
}
