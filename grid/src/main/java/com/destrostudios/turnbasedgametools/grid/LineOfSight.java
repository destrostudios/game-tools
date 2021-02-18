package com.destrostudios.turnbasedgametools.grid;

import java.util.function.Predicate;

public class LineOfSight {

    public boolean inLineOfSight(Predicate<Position> isSeeThrough, Position source, Position target) {
        if (source.equals(target)) {
            return true;
        }
        // https://www.redblobgames.com/grids/line-drawing.html#supercover
        int dx = target.x - source.x;
        int dy = target.y - source.y;

        int nx = Math.abs(dx);
        int ny = Math.abs(dy);

        int sign_x = dx > 0 ? 1 : -1;
        int sign_y = dy > 0 ? 1 : -1;

        int x = source.x;
        int y = source.y;
        int ix = 0;
        int iy = 0;
        while (true) {
            int decision = (1 + 2 * ix) * ny - (1 + 2 * iy) * nx;
            if (decision == 0) {
                // next step is diagonal
                x += sign_x;
                y += sign_y;
                ix++;
                iy++;
            } else if (decision < 0) {
                // next step is horizontal
                x += sign_x;
                ix++;
            } else {
                // next step is vertical
                y += sign_y;
                iy++;
            }
            if (x == target.x && y == target.y) {
                return true;
            }
            if (!isSeeThrough.test(new Position(x, y))) {
                return false;
            }
        }
    }
}
