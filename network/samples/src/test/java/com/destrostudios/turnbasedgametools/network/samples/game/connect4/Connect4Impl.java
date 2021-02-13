package com.destrostudios.turnbasedgametools.network.samples.game.connect4;

public class Connect4Impl {

    private final long row_0;
    private final long col_0;
    private final long board;

    public final int width, height;
    public long own, opp;

    public Connect4Impl(int width, int height) {
        this.width = width;
        this.height = height;
        int bufferedHeight = height + 1;
        row_0 = ((1L << width * bufferedHeight) - 1) / ((1L << bufferedHeight) - 1);
        col_0 = (1L << height) - 1;
        board = row_0 * col_0;
    }

    public boolean isWhiteActive() {
        return (Long.bitCount(own | opp) & 1) == 0;
    }

    public long white() {
        if (isWhiteActive()) {
            return own;
        }
        return opp;
    }

    public long black() {
        if (isWhiteActive()) {
            return opp;
        }
        return own;
    }

    public void move(long move) {
        long tmp = opp;
        opp = own | move;
        own = tmp;
    }

    public long availableMoves() {
        return ((own | opp) + row_0) & board;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                long flag = (1L << ((x * (height + 1)) + y));
                builder.append('[');
                if ((flag & white()) != 0) {
                    builder.append('x');
                } else if ((flag & black()) != 0) {
                    builder.append('o');
                } else {
                    builder.append(' ');
                }
                builder.append(']');
            }
            if (y != 0) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

}
