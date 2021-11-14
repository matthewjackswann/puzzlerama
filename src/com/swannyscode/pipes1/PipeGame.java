package com.swannyscode.pipes1;

import com.swannyscode.Constants;
import com.swannyscode.RestrictedRobot;
import com.swannyscode.util.Tuple3;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipeGame {
    private final RestrictedRobot robot;
    private final int delay = Constants.getIntConstant("pipeLevelDelay");

    public PipeGame(RestrictedRobot robot) {
        this.robot = robot;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public long play() {
        long solveTimeTaken = 0;
        try {
            while (true) {
                PipeGrid toSolve = nextLevel(); // loads the next level to be solved
                // instructions for the correct solution are put onto the instructions queue by the controller
                BlockingQueue<Tuple3<AtomicBoolean, Pipe, Integer>> instructions = new LinkedBlockingQueue<>();
                PipeController controller = new PipeController(instructions, robot);
                controller.start(); // starts the controller so that instructions can begin being executed as soon as they are added
                long startTime = System.currentTimeMillis(); // times how long the solve takes
                System.out.println(toSolve.solve(instructions));
                solveTimeTaken += System.currentTimeMillis() - startTime;
                try {
                    instructions.put(new Tuple3<>(new AtomicBoolean(true), Pipe.nullPipe, 0)); // adds terminating instruction, stopping the controller thread
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("couldn't put terminating instruction into queue");
                }
                try {
                    controller.join(); // waits for the controller to have processed all the instructions before loading the next level
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("couldn't wait for controller to stop");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return solveTimeTaken;
    }

    private void levelDelay() { // the delay taken between levels
        try {
            TimeUnit.MILLISECONDS.sleep(this.delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("couldn't delay between levels");
        }
    }

    // gets the nextLevel from the robots capture, is called recursively until a level is found or a stack overflow
    private PipeGrid nextLevel() {
        levelDelay();
        BufferedImage capture = this.robot.createScreenCapture();
        try {
            PipeGrid grid = PipeProcessor.getCurrentState(capture); // tries to load a grid from the current screen
            if (grid.solved()) throw new RuntimeException("grid already solved");
            TimeUnit.MILLISECONDS.sleep(200); // added delay so the capture isn't taken during the animation
            grid = PipeProcessor.getCurrentState(capture);
            return grid; // only returns when a valid grid is found
        } catch (Exception e) { // if there is not valid grid then call recursively
            return nextLevel();
        }
    }
}
