package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Pipe {

    abstract boolean update(PipeDirection direction, PipeColour colour);
    abstract boolean isSatisfied();
    abstract void reset();
    abstract List<PipeDirection> getConnections();

    private final Point2D center;
    final Map<PipeDirection, Pipe> adjacentPipes = new HashMap<>();
    public Pipe(Point2D center) {
        this.center = center;
        for (PipeDirection direction : PipeDirection.values()) {
            adjacentPipes.put(direction, nullPipe);
        }
    }

    public Point2D getCenter() {
        return center;
    }

    public void pair(PipeDirection direction, Pipe pipe) {
        adjacentPipes.put(direction, pipe);
    }

    public int getRotation() {return -1;}

    public int getMaxRotation() {
        return -1;
    }

    void rotate() {}

    void setRotation(int x) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pipe)) return false;

        Pipe pipe = (Pipe) o;

        return center.equals(pipe.center);
    }

    @Override
    public int hashCode() {
        return center.hashCode();
    }

    List<Integer> getValidRotations(List<Pipe> unFixed) {
        List<Integer> validRotations = new ArrayList<>();
        for (int rotation = 0; rotation < this.getMaxRotation(); rotation++) {
            boolean rotationValid = true;
            for (PipeDirection neighborDir : PipeDirection.values()) {
                Pipe neighbor = this.adjacentPipes.get(neighborDir);
                if (!unFixed.contains(neighbor)) { // if neighbor is fixed
                    if (neighbor.getConnections().contains(neighborDir.opposite()) != getConnections().contains(neighborDir)) rotationValid = false;
                }
            }
            if (rotationValid) validRotations.add(this.getRotation());
            this.rotate();
        }
        return validRotations;
    }

    static class PipeState {
        private final PipeColour defaultState;
        private PipeColour state;
        private final List<PipeDirection> connecting;

        PipeState(List<PipeDirection> connecting) {
            this(PipeColour.CLEAR, connecting);
        }

        public PipeState(PipeColour defaultState, List<PipeDirection> connecting) {
            this.defaultState = defaultState;
            this.state = defaultState;
            this.connecting = connecting;
        }

        void updateState(PipeColour colour) {
            this.state = this.state.add(colour);
        }

        void reset() {
            this.state = defaultState;
        }

        PipeColour getState() {
            return state;
        }

        List<PipeDirection> getConnecting() {
            return new ArrayList<>(this.connecting);
        }
    }

    final static Pipe nullPipe = new Pipe(new Point2D(-1,-1)) {

        @Override
        boolean update(PipeDirection direction, PipeColour colour) {
            return false;
        }

        @Override
        boolean isSatisfied() {
            return true;
        }

        @Override
        void reset() {}

        @Override
        List<PipeDirection> getConnections() {
            return new ArrayList<>();
        }

        @Override
        public String toString() {
            return "null";
        }
    };
}
