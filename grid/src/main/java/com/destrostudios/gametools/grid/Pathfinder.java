package com.destrostudios.gametools.grid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public class Pathfinder {

    /**
     * @param isPathable predicate that returns whether a tile can be used for pathing or is an obstacle
     * @param start      start of the path
     * @param end        target position of the path
     * @return list of path positions (start exclusive) if found
     */
    public Optional<List<Position>> findPath(Predicate<Position> isPathable, Position start, Position end) {
        return findPath(isPathable, start, end, Integer.MAX_VALUE);
    }

    /**
     * @param isPathable predicate that returns whether a tile can be used for pathing or is an obstacle
     * @param start      start of the path
     * @param end        target position of the path
     * @param maxCost    upper bound of maximal path length
     * @return list of path positions (start exclusive) if found
     */
    public Optional<List<Position>> findPath(Predicate<Position> isPathable, Position start, Position end, int maxCost) {
        return findPath(isPathable, start, end, new ManhattanDistance(), maxCost);
    }

    /**
     * @param isPathable     predicate that returns whether a tile can be used for pathing or is an obstacle
     * @param start          start of the path
     * @param end            target position of the path
     * @param distanceMetric distance metric used
     * @param maxCost        upper bound of maximal path length
     * @return list of path positions (start exclusive) if found
     */
    public Optional<List<Position>> findPath(Predicate<Position> isPathable, Position start, Position end, DistanceMetric distanceMetric, int maxCost) {
        return findPathIntoNeighborhood(isPathable, start, List.of(end), distanceMetric, 0, maxCost);
    }

    /**
     * @param isPathable       predicate that returns whether a tile can be used for pathing or is an obstacle
     * @param start            start of the path
     * @param neighborTargets  targets to be used to path towards, the path will end in their neighborhood
     * @param distanceMetric   distance metric used
     * @param neighborhoodSize how close two positions must be to be considered neighbors
     * @param maxCost          upper bound of maximal path length
     * @return list of path positions (start exclusive) if found
     */
    public Optional<List<Position>> findPathIntoNeighborhood(Predicate<Position> isPathable, Position start, List<Position> neighborTargets, DistanceMetric distanceMetric, int neighborhoodSize, int maxCost) {
        // https://www.redblobgames.com/pathfinding/a-star/implementation.html#python-astar

        PriorityQueue<PriorityItem<Position, Integer>> frontier = new PriorityQueue<>();
        Map<Position, Position> came_from = new HashMap<>();
        Map<Position, Integer> cost_so_far = new HashMap<>();

        frontier.add(new PriorityItem<>(start, 0));
        cost_so_far.put(start, 0);

        while (!frontier.isEmpty()) {
            Position current = frontier.poll().item;
            for (Position neighborTarget : neighborTargets) {
                if (distanceMetric.distanceBetween(current, neighborTarget) <= neighborhoodSize) {
                    // path found
                    return Optional.of(collectPath(current, came_from));
                }
            }

            int new_cost = cost_so_far.get(current) + 1;
            if (new_cost <= maxCost) {
                for (Position next : distanceMetric.neighbors(current)) {
                    Integer nextCost = cost_so_far.get(next);
                    if ((nextCost == null && isPathable.test(next))
                            || (nextCost != null && new_cost < nextCost)) {
                        cost_so_far.put(next, new_cost);
                        int priority = new_cost + neighborTargets.stream()
                                .mapToInt(p -> distanceMetric.distanceBetween(next, p))
                                .min().orElseThrow();
                        frontier.add(new PriorityItem<>(next, priority));
                        came_from.put(next, current);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private List<Position> collectPath(Position end, Map<Position, Position> came_from) {
        List<Position> path = new LinkedList<>();
        Position step = end;
        while (came_from.containsKey(step)) {
            path.addFirst(step);
            step = came_from.get(step);
        }
        return path;
    }
}
