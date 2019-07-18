package de.codesourcery.quix;

enum Direction
{
    LEFT(-1, 0),
    RIGHT(1, 0),
    UP(0, -1),
    DOWN(0, 1);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy)
    {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction opposite() {
        switch(this) {

            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            default:
                throw new RuntimeException("Unhandled direction: "+this);
        }
    }
}
