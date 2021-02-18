package com.destrostudios.turnbasedgametools.grid;

public interface Heuristic {

    int estimateCost(Position start, Position end);
}
