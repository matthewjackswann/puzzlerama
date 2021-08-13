package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Source extends Pipe {

    private final PipeDirection outDirection;
    private final PipeColour colour;

    public Source(Point2D center, PipeDirection outDirection, PipeColour colour) {
        super(center);
        this.outDirection = outDirection;
        this.colour = colour;
    }

    @Override
    boolean update(PipeDirection direction, PipeColour colour) {
        return true;
    }

    boolean update() {
        return adjacentPipes.get(outDirection).update(outDirection.opposite(), colour);
    }

    @Override
    boolean isSatisfied() {
        return true;
    }

    @Override
    void reset() {}

    @Override
    List<PipeDirection> getConnections() {
        return new ArrayList<>(Collections.singleton(outDirection));
    }
}
