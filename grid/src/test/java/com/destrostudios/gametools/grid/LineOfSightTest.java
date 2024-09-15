package com.destrostudios.gametools.grid;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineOfSightTest {

    private final LineOfSight lineOfSight = new LineOfSight();

    @Test
    public void diagonalNeighbors() {
        Predicate<Position> isSeeThrough = p -> false;
        for (int i = 0; i < 8; i++) {
            Position source = permutate(3, 4, i);
            Position target = permutate(4, 5, i);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source);
        }
    }

    @Test
    public void directNeighbors() {
        Predicate<Position> isSeeThrough = p -> false;
        for (int i = 0; i < 8; i++) {
            Position source = permutate(-4, 4, i);
            Position target = permutate(-4, 5, i);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source);
        }
    }

    @Test
    public void unblockedDiagonal() {
        for (int i = 0; i < 8; i++) {
            Position position = permutate(2, 2, i);
            Predicate<Position> isSeeThrough = p -> position.equals(p);
            Position source = permutate(1, 1, i);
            Position target = permutate(3, 3, i);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source);
        }
    }

    @Test
    public void blockedDiagonal() {
        for (int i = 0; i < 8; i++) {
            Position position = permutate(2, 2, i);
            Predicate<Position> isSeeThrough = p -> !position.equals(p);
            Position source = permutate(1, 1, i);
            Position target = permutate(3, 3, i);
            assertFalse(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target);
            assertFalse(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source);
        }
    }


    @Test
    public void unblockedAccrossOrigin() {
        for (int i = 0; i < 8; i++) {
            List<Position> traversedPositions = List.of(
                    permutate(-1, 1, i),
                    permutate(-1, 0, i),
                    permutate(0, 0, i),
                    permutate(1, 0, i),
                    permutate(1, -1, i),
                    permutate(2, -1, i),
                    permutate(3, -1, i),
                    permutate(3, -2, i)
            );
            Predicate<Position> isSeeThrough = p -> traversedPositions.contains(p);
            Position source = permutate(-2, 1, i);
            Position target = permutate(4, -2, i);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target);
            assertTrue(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source);
        }
    }

    @Test
    public void blockedAccrossOrigin() {
        for (int i = 0; i < 8; i++) {
            List<Position> traversedPositions = List.of(
                    permutate(-1, -1, i),
                    permutate(-1, 0, i),
                    permutate(0, 0, i),
                    permutate(1, 0, i),
                    permutate(1, 1, i),
                    permutate(2, 1, i),
                    permutate(3, 1, i),
                    permutate(3, 2, i)
            );
            for (Position blocked : traversedPositions) {
                Predicate<Position> isSeeThrough = p -> !blocked.equals(p);
                Position source = permutate(-2, -1, i);
                Position target = permutate(4, 2, i);
                assertFalse(lineOfSight.inLineOfSight(isSeeThrough, source, target), source + " -> " + target + " blocked: " + blocked);
                assertFalse(lineOfSight.inLineOfSight(isSeeThrough, target, source), target + " -> " + source + " blocked: " + blocked);
            }
        }
    }

    private static Position permutate(int x, int y, int permutation) {
        return permutate(new Position(x, y), permutation);
    }

    private static Position permutate(Position position, int permutation) {
        int x = position.x;
        int y = position.y;
        switch (permutation) {
            case 0:
                return new Position(x, y);
            case 1:
                return new Position(-y, x);
            case 2:
                return new Position(-x, -y);
            case 3:
                return new Position(y, -x);
            case 4:
                return new Position(-x, y);
            case 5:
                return new Position(-y, -x);
            case 6:
                return new Position(x, -y);
            case 7:
                return new Position(y, x);
            default:
                throw new AssertionError(permutation);
        }
    }
}
