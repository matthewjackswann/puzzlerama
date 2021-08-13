package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.Point2D;
import com.matthewjackswann.util.Tuple2;
import com.matthewjackswann.util.Tuple3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

// representation of an entire level / puzzle
public class PipeGrid {
    private final Pipe[][] grid; // n x n size
    private final int n;
    private final List<Source> sources = new ArrayList<>(); // list of taps / sources
    private final static Map<Point2D, PipeDirection> directionMap= Map.of(
            new Point2D(0, -1), PipeDirection.NORTH,
            new Point2D(1, 0), PipeDirection.EAST,
            new Point2D(0, 1), PipeDirection.SOUTH,
            new Point2D(-1, 0), PipeDirection.WEST);

    public PipeGrid(int n, List<Pipe> pipes) {
        if (n < 1) throw new RuntimeException("n can't be smaller then 1");
        this.grid = new Pipe[n][n];
        this.n = n;
        if (pipes.size() != n * n) throw new RuntimeException("Grid size doesn't match pipe number");
        for (int i = 0; i < pipes.size(); i++) {
            int y = i % n;
            int x = Math.floorDiv(i,n);
            if (pipes.get(i) instanceof Source) sources.add((Source) pipes.get(i));
            this.grid[x][y] = pipes.get(i);
        }
        if (sources.isEmpty()) throw new RuntimeException("PipeGrid must have at least one source/tap");
        for (int y = 0; y < this.n; y++) { // pairs all the pipes
            for (int x = 0; x < this.n; x++) {
                if (x >= 1) this.grid[x][y].pair(PipeDirection.WEST, this.grid[x-1][y]);
                if (x < this.n - 1) this.grid[x][y].pair(PipeDirection.EAST, this.grid[x+1][y]);
                if (y >= 1) this.grid[x][y].pair(PipeDirection.NORTH, this.grid[x][y-1]);
                if (y < this.n - 1) this.grid[x][y].pair(PipeDirection.SOUTH, this.grid[x][y+1]);
            }
        }
    }

    private void clear() {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                this.grid[x][y].reset();
            }
        }
    }

    public boolean solved() {
        if (!simulate()) return false;
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (!this.grid[x][y].isSatisfied()) return false;
            }
        }
        return true;
    }

    private boolean solve(List<Point2D> unFixed,
                          BlockingQueue<Tuple3<Boolean[], Point2D, Integer>> instructionQueue,
                          Boolean[] valid) {
        if (unFixed.isEmpty()) return solved();
        boolean partFixed = true;
        while (partFixed) {
            partFixed = false;
            List<Point2D> toFix = new ArrayList<>();
            for (Point2D coord : unFixed) {
                Connector pipe = (Connector) grid[coord.getX()][coord.getY()];
                List<Integer> validRotations = new ArrayList<>();
                for (int rotation = 0; rotation < pipe.getMaxRotation(); rotation++) {
                    pipe.rotate();
                    boolean rotationValid = true;
                    for (Point2D neighborDir : directionMap.keySet()) {
                        if (!unFixed.contains(coord.add(neighborDir))) { // if direction is fixed
                            if (pipe.isConnectedIncorrectlyTo(directionMap.get(neighborDir))) rotationValid = false;
                        }
                    }
                    if (rotationValid) validRotations.add(pipe.getRotation());
                }
                if (validRotations.size() == 1) {
                    pipe.setRotation(validRotations.get(0));
                    try {
                        instructionQueue.put(new Tuple3<>(valid, coord, validRotations.get(0)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Couldn't add instruction to queue");
                    }
                    partFixed = true;
                    toFix.add(coord);
                }
            }
            unFixed.removeIf(toFix::contains);
            toFix.clear();
        }
        if (unFixed.isEmpty()) return solved();
        // otherwise makes assumption
        Point2D coord = unFixed.get(0);
        unFixed.remove(coord);
        Connector pipe = (Connector) grid[coord.getX()][coord.getY()];
        for (int rotation = 0; rotation < pipe.getMaxRotation(); rotation++) {
            boolean rotationValid = true;
            for (Point2D neighborDir : directionMap.keySet()) {
                if (!unFixed.contains(coord.add(neighborDir))) { // if direction is fixed
                    if (pipe.isConnectedIncorrectlyTo(directionMap.get(neighborDir))) rotationValid = false;
                }
            }
            if (rotationValid) {
                Boolean[] correct = {true};
                if (solve(new ArrayList<>(unFixed), instructionQueue, correct)) {
                    try {
                        instructionQueue.put(new Tuple3<>(correct, coord, rotation));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Couldn't add instruction to queue");
                    }
                    return true;
                } else {
                    correct[0] = false;
                }
            }
            pipe.rotate();
        }
        return false;
    }

    public Map<Point2D, Tuple2<Point2D, Integer>> getPipeInfo() {
        Map<Point2D, Tuple2<Point2D, Integer>> results = new HashMap<>();
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                Pipe p = grid[x][y];
                if (p.getRotation() != -1) {
                    Connector connector = (Connector) p;
                    results.put(new Point2D(x,y), new Tuple2<>(connector.getCenter(), connector.getMaxRotation()));
                }
            }
        }
        return results;
    }

    public boolean solve(BlockingQueue<Tuple3<Boolean[], Point2D, Integer>> instructions) {
        List<Point2D> unFixed = new ArrayList<>();
        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                if (grid[x][y].getRotation() != -1) {
                    unFixed.add(new Point2D(x,y));
                }
            }
        }
        return solve(unFixed, instructions, new Boolean[]{true});
    }

    private boolean simulate() {
        clear();
        for (Source s : sources) {
            if (!s.update()) return false;
        }
        return true;
    }
}
