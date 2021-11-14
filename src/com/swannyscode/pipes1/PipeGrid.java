package com.swannyscode.pipes1;

import com.swannyscode.util.Tuple3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

// representation of an entire level / puzzle
public class PipeGrid {
    private final Pipe[][] grid; // n x n size
    private final int n;
    private final List<Source> sources = new ArrayList<>(); // list of taps / sources

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

    private boolean solve(List<Pipe> unFixed,
                          BlockingQueue<Tuple3<AtomicBoolean, Pipe, Integer>> instructionQueue,
                          AtomicBoolean valid) {
        if (unFixed.isEmpty()) return solved();
        boolean partFixed = true;
        while (partFixed) {
            partFixed = false;
            List<Pipe> toFix = new ArrayList<>();
            for (Pipe pipe : unFixed) {
                List<Integer> validRotations = pipe.getValidRotations(unFixed);
                if (validRotations.size() == 1) {
                    pipe.setRotation(validRotations.get(0));
                    try {
                        instructionQueue.put(new Tuple3<>(valid, pipe, validRotations.get(0)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Couldn't add instruction to queue");
                    }
                    partFixed = true;
                    toFix.add(pipe);
                }
            }
            unFixed.removeIf(toFix::contains);
        }
        if (unFixed.isEmpty()) return solved();
        // otherwise makes assumption
        Pipe newFixedPipe = unFixed.get(0);
        unFixed.remove(newFixedPipe);
        List<Integer> validRotations = newFixedPipe.getValidRotations(unFixed);
        for (int rotation: validRotations) {
            newFixedPipe.setRotation(rotation);
            AtomicBoolean correct = new AtomicBoolean(true);
            if (solve(new ArrayList<>(unFixed), instructionQueue, correct)) {
                try {
                    instructionQueue.put(new Tuple3<>(correct, newFixedPipe, rotation));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Couldn't add instruction to queue");
                }
                return true;
            } else {
                correct.set(false);
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean solve(List<Pipe> unFixed) {
        if (unFixed.isEmpty()) return solved(); // used for recursive call, if everything is fixed return if it is solved
        boolean partFixed = true;
        while (partFixed) { // while at least one pipe was fixed the previous iteration
            partFixed = false;
            List<Pipe> toFix = new ArrayList<>();
            for (Pipe pipe : unFixed) {
                List<Integer> validRotations = pipe.getValidRotations(unFixed);
                if (validRotations.size() == 1) { // if there is only one valid rotation
                    pipe.setRotation(validRotations.get(0)); // set it's rotation to that
                    partFixed = true;
                    toFix.add(pipe); // fix the pipe
                }
            }
            unFixed.removeIf(toFix::contains); // remove the fixed pipes from the unfixed list
        }
        if (unFixed.isEmpty()) return solved();
        // otherwise makes assumption
        Pipe newFixedPipe = unFixed.get(0); // selects the first unfixed pipe
        unFixed.remove(newFixedPipe); // removes it from the unfixed list
        List<Integer> validRotations = newFixedPipe.getValidRotations(unFixed);
        for (int rotation: validRotations) {
            newFixedPipe.setRotation(rotation); // presumes that this rotation is the correct one
            if (solve(new ArrayList<>(unFixed))) { // if the grid can be solved then the presumption is correct
                return true;
            } // if the grid can't be solved the grid is incorrect and moves onto the next valid rotation
        }
        // if all valid rotations are invalid then a previous assumption must be wrong so false is returned
        // this will move the assumption on to another one
        return false;
    }

    public boolean solve(BlockingQueue<Tuple3<AtomicBoolean, Pipe, Integer>> instructions) {
        List<Pipe> unFixed = new ArrayList<>();
        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                Pipe pipe = grid[x][y];
                if (pipe.getMaxRotation() != -1) {
                    unFixed.add(pipe);
                }
            }
        }
        return solve(unFixed, instructions, new AtomicBoolean(true));
    }

    private boolean simulate() {
        clear();
        for (Source s : sources) {
            if (!s.update()) return false;
        }
        return true;
    }
}
