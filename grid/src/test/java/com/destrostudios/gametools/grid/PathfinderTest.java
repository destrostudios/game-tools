package com.destrostudios.gametools.grid;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathfinderTest {

    private final Pathfinder pathfinder = new Pathfinder();

    @Test
    public void findPathIntoNeighborhood() {
        Optional<List<Position>> path = pathfinder.findPathIntoNeighborhood(x -> true, new Position(0, 0), List.of(new Position(3, 0)), new ManhattanDistance(), 1, 100);
        assertTrue(path.isPresent());
        assertEquals(List.of(new Position(1, 0), new Position(2, 0)), path.get());
    }

    @Test
    public void findPath() {
        // S = start, T = target, # = obstacle, . = walkable, origin (0,0) is bottom left
        //
        // . . . . .
        // . # T # .
        // . . . # .
        // . # # # .
        // . # . . .
        // . # . . .
        // . . S . .
        // . . . . .

        Grid grid = new Grid(5, 8);
        grid.setObstacle(new Position(1, 2), true);
        grid.setObstacle(new Position(1, 3), true);
        grid.setObstacle(new Position(1, 4), true);
        grid.setObstacle(new Position(1, 6), true);
        grid.setObstacle(new Position(2, 4), true);
        grid.setObstacle(new Position(3, 4), true);
        grid.setObstacle(new Position(3, 5), true);
        grid.setObstacle(new Position(3, 6), true);

        Optional<List<Position>> path = pathfinder.findPath(grid::isWalkable, new Position(2, 1), new Position(2, 6));
        assertTrue(path.isPresent());
        assertEquals(List.of(
                new Position(1, 1),
                new Position(0, 1),
                new Position(0, 2),
                new Position(0, 3),
                new Position(0, 4),
                new Position(0, 5),
                new Position(1, 5),
                new Position(2, 5),
                new Position(2, 6)
        ), path.get());
    }

    @Test
    public void noPath() {
        Grid grid = new Grid(3, 1);
        grid.setObstacle(new Position(1, 0), true);
        Optional<List<Position>> path = pathfinder.findPath(grid::isWalkable, new Position(0, 0), new Position(2, 0));
        assertFalse(path.isPresent());
    }

    @Test
    public void startIsDestination() {
        Position position = new Position(0, 0);
        Optional<List<Position>> path = pathfinder.findPath(p -> false, position, position);
        assertTrue(path.isPresent());
        assertTrue(path.get().isEmpty());
    }

    @Test
    public void pathTooLong() {
        Optional<List<Position>> path = pathfinder.findPath(p -> true, new Position(0, 0), new Position(100, 0), 50);
        assertFalse(path.isPresent());
    }
}
