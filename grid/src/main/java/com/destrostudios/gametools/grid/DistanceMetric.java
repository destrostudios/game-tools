package com.destrostudios.gametools.grid;

public interface DistanceMetric {

    int distanceBetween(Position start, Position end);

    Position[] neighbors(Position source);
}
