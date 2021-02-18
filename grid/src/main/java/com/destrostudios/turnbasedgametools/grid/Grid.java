package com.destrostudios.turnbasedgametools.grid;

public class Grid {

    private final int width, height;
    private final boolean[] obstacles;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        obstacles = new boolean[width * height];
    }

    public void setObstacle(Position position, boolean value) {
        obstacles[index(position)] = value;
    }

    public boolean isWalkable(Position position) {
        return inBounds(position) && !isObstacle(position);
    }

    public boolean isObstacle(Position position) {
        return obstacles[index(position)];
    }

    public boolean inBounds(Position position) {
        return 0 <= position.x && position.x < width && 0 <= position.y && position.y < height;
    }

    private int index(Position position) {
        return position.x + width * position.y;
    }
}
