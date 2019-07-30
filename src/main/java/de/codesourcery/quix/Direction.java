package de.codesourcery.quix;

enum Direction
{
    LEFT(-1, 0) {
        @Override
        public Direction opposite()
        {
            return RIGHT;
        }
    },
    RIGHT(1, 0) {
        @Override
        public Direction opposite()
        {
            return LEFT;
        }
    },
    UP(0, -1) {
        @Override
        public Direction opposite()
        {
            return DOWN;
        }
    },
    DOWN(0, 1) {
        @Override
        public Direction opposite()
        {
            return UP;
        }
    };

    public final int dx;
    public final int dy;

    Direction(int dx, int dy)
    {
        this.dx = dx;
        this.dy = dy;
    }

    public abstract Direction opposite();
}
