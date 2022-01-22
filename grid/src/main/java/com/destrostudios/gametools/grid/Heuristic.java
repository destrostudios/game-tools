package com.destrostudios.gametools.grid;

public interface Heuristic {

    int estimateCost(Position start, Position end);
}
