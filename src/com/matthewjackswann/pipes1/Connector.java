package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connector extends Pipe {

    private final Map<PipeDirection, PipeState> connections = new HashMap<>();
    private final Map<PipeDirection, PipeDirection> rotationMapIn = new HashMap<>();
    private final Map<PipeDirection, PipeDirection> rotationMapOut = new HashMap<>();
    private int rotation = 0;
    private final int maxRotation;

    public Connector(Point2D center, List<List<PipeDirection>> links, int rotation) {
        super(center);
        this.maxRotation = rotation;
        for (List<PipeDirection> link : links) {
            PipeState state = new PipeState(link);
            for (PipeDirection d : link) {
                connections.put(d, state);
            }
        }
        for (PipeDirection d : PipeDirection.values()) {
            rotationMapIn.put(d, d);
            rotationMapOut.put(d, d);
        }
    }

    @Override
    public int getMaxRotation() {
        return maxRotation;
    }

    void rotate() { // rotates clockwise
        if (maxRotation == 0) throw new RuntimeException("Can't rotate locked pipe");
        this.rotation = ((this.rotation + 1) % maxRotation);
        for (PipeDirection d : PipeDirection.values()) {
            rotationMapIn.put(d, rotationMapIn.get(d).left());
            rotationMapOut.put(d, rotationMapOut.get(d).right());
        }
    }

    public void setRotation(int r) {
        if (r > maxRotation && r >= 0) throw new RuntimeException("can't rotate connector that much");
        while (this.rotation != r) {
            rotate();
        }
    }

    @Override
    public int getRotation() {
        if (maxRotation == -1) return -1;
        return rotation;
    }

    @Override
    boolean update(PipeDirection direction, PipeColour colour) {
        if (colour == PipeColour.INVALID || colour == PipeColour.CLEAR) return false;
        PipeDirection rawDirection = rotationMapIn.get(direction);
        PipeState state = connections.getOrDefault(rawDirection, null);
        if (state == null) return false; // pipes into side with no connections
        if (state.getState() == colour) return true;
        state.updateState(colour);
        if (state.getState() == PipeColour.INVALID) return false;
        for (PipeDirection connectedPipe : state.getConnecting()) {
            PipeDirection out = rotationMapOut.get(connectedPipe);
            if (out == null) throw new RuntimeException("Rotation map shouldn't return null");
            if (!adjacentPipes.get(out).update(out.opposite(), state.getState())) return false;
        }
        return true;
    }

    @Override
    boolean isSatisfied() {
        for (PipeState state : connections.values()) {
            if (state.getState() == PipeColour.INVALID || state.getState() == PipeColour.CLEAR) {
                return false;
            }
        }
        return true;
    }

    @Override
    void reset() {
        for (PipeState state : connections.values()) {
            state.reset();
        }
    }

    @Override
    List<PipeDirection> getConnections() {
        List<PipeDirection> result = new ArrayList<>();
        for (PipeState state: connections.values()) {
            for (PipeDirection rawDirection: state.getConnecting()) {
                PipeDirection rotatedDirection = rotationMapOut.get(rawDirection);
                if (!result.contains(rotatedDirection)) {
                    result.add(rotatedDirection);
                }
            }
        }
        return result;
    }
}
