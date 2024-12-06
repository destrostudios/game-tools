package com.destrostudios.gametools.grid;

public class ManhattanDistance implements DistanceMetric {
    @Override
    public int distanceBetween(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @Override
    public Position[] neighbors(Position source) {
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
