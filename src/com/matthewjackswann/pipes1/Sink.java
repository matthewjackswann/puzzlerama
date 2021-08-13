package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sink extends Pipe {

    private final PipeDirection inDirection;
    private final PipeColour targetColour;
    private final PipeState state = new PipeState(new ArrayList<>());

    public Sink(Point2D center, PipeDirection inDirection, PipeColour targetColour) {
        super(center);
        this.inDirection = inDirection;
        this.targetColour = targetColour;
    }

    @Override
    boolean update(PipeDirection direction, PipeColour colour) {
        if (inDirection != direction) return false;
        state.updateState(colour);
        return colour.add(targetColour) == targetColour;
    }

    @Override
    boolean isSatisfied() {
        return state.getState() == targetColour;
    }

    @Override
    void reset() {
        state.reset();
    }

    @Override
    List<PipeDirection> getConnections() {
        return new ArrayList<>(Collections.singleton(inDirection));
    }
}
