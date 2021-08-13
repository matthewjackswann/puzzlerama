package com.matthewjackswann.pipes1;

public enum PipeDirection {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public PipeDirection opposite() {
        if (this == NORTH) return SOUTH;
        if (this == EAST) return WEST;
        if (this == SOUTH) return NORTH;
        if (this == WEST) return EAST;
        return null;
    }

    public PipeDirection left() {
        if (this == NORTH) return WEST;
        if (this == EAST) return NORTH;
        if (this == SOUTH) return EAST;
        if (this == WEST) return SOUTH;
        return null;
    }

    public PipeDirection right() {
        if (this == NORTH) return EAST;
        if (this == EAST) return SOUTH;
        if (this == SOUTH) return WEST;
        if (this == WEST) return NORTH;
        return null;
    }

    public static PipeDirection fromIndex(int i) {
        if (i == 0) return NORTH;
        if (i == 1) return EAST;
        if (i == 2) return SOUTH;
        if (i == 3) return WEST;
        return null;
    }
}
