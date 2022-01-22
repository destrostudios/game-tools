package com.destrostudios.gametools.grid;

public class ManhattanHeuristic implements Heuristic {
    @Override
    public int estimateCost(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
