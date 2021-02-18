package com.destrostudios.turnbasedgametools.grid;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public class Pathfinder {


    public Optional<List<Position>> findPath(Predicate<Position> isWalkable, Position start, Position end) {
        return findPath(isWalkable, start, end, new ManhattanHeuristic());
    }

    /**
     * @param isWalkable
     * @param start
     * @param end
     * @param heuristic
     * @return list of path positions (start exclusive & end inclusive) if found
     */
    public Optional<List<Position>> findPath(Predicate<Position> isWalkable, Position start, Position end, Heuristic heuristic) {
        // https://www.redblobgames.com/pathfinding/a-star/implementation.html#python-astar

        PriorityQueue<PriorityItem<Position, Integer>> frontier = new PriorityQueue<>();
        Map<Position, Position> came_from = new HashMap<>();
        Map<Position, Integer> cost_so_far = new HashMap<>();

        frontier.add(new PriorityItem<>(start, 0));
        cost_so_far.put(start, 0);

        while (!frontier.isEmpty()) {
            Position current = frontier.poll().item;
            if (current.equals(end)) {
                // path found, collect positions
                List<Position> path = new LinkedList<>();
                Position step = end;
                while (came_from.containsKey(step)) {
                    path.add(0, step);
                    step = came_from.get(step);
                }
                return Optional.of(path);
            }

            for (Position next : neighbors(current)) {
                if (!isWalkable.test(next)) {
                    continue;
                }
                int new_cost = cost_so_far.get(current) + 1;
                if (!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)) {
                    cost_so_far.put(next, new_cost);
                    int priority = new_cost + heuristic.estimateCost(next, end);
                    frontier.add(new PriorityItem<>(next, priority));
                    came_from.put(next, current);
                }
            }
        }
        return Optional.empty();
    }

    private Position[] neighbors(Position source) {
        Position[] result = new Position[4];
        for (int i = 0; i < 4; i++) {
            int nX, nY;
            switch (i) {
                case 0:
                    nX = source.x;
                    nY = source.y + 1;
                    break;
                case 1:
                    nX = source.x + 1;
                    nY = source.y;
                    break;
                case 2:
                    nX = source.x;
                    nY = source.y - 1;
                    break;
                case 3:
                    nX = source.x - 1;
                    nY = source.y;
                    break;
                default:
                    throw new AssertionError(i);
            }
            result[i] = new Position(nX, nY);
        }
        return result;
    }
}
