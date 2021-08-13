package com.matthewjackswann.pipes1;

import com.matthewjackswann.Constants;
import com.matthewjackswann.RestrictedRobot;
import com.matthewjackswann.util.Point2D;
import com.matthewjackswann.util.Tuple3;

import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PipeGame {
    private int levelNo;
    private final RestrictedRobot robot;
    private final int delay = Constants.getIntConstant("pipeLevelDelay");
    private final Tuple3<Integer, Integer, Integer> levelButtonColour;
    private final int targetRange = Constants.getIntConstant("targetRange");
    private final boolean autoNextLevel;

    public PipeGame(int levelNo, RestrictedRobot robot, boolean autoNextLevel) {
        if (levelNo < 1) throw new RuntimeException("The number of levels must be greater then 1");
        int maxLevelAllowed = Constants.getIntConstant("maxPipeLevel");
        if (levelNo > maxLevelAllowed) throw new RuntimeException("There are only " + maxLevelAllowed + " levels");
        this.levelNo = levelNo;
        this.robot = robot;
        this.autoNextLevel = autoNextLevel;
        int buttonArrowRGB = Constants.getIntConstant("nextLevelButtonColour");
        this.levelButtonColour = new Tuple3<>((buttonArrowRGB >> 16) & 0xFF, (buttonArrowRGB >> 8) & 0xFF, (buttonArrowRGB) & 0xFF);
    }

    public long play() {
        long solveTimeTaken = 0;
        // locate next level button
        Point2D levelButtonPos = getLevelButton();
        for (; this.levelNo >= 0; this.levelNo--) {
            PipeGrid toSolve = nextLevel(levelButtonPos); // loads the next level to be solved
            System.out.println("Remaining levels: " + this.levelNo);
            // instructions for the correct solution are put onto the instructions queue by the controller
            BlockingQueue<Tuple3<Boolean[], Point2D, Integer>> instructions = new LinkedBlockingQueue<>();
            PipeController controller = new PipeController(instructions, toSolve.getPipeInfo(), robot);
            controller.start(); // starts the controller so that instructions can begin being executed as soon as they are added
            long startTime = System.currentTimeMillis(); // times how long the solve takes
            toSolve.solve(instructions);
            solveTimeTaken += System.currentTimeMillis() - startTime;
            try {
                instructions.put(new Tuple3<>(new Boolean[]{true}, new Point2D(-1,-1), 0)); // adds terminating instruction, stopping the controller thread
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
    private PipeGrid nextLevel(Point2D levelButtonPos) {
        levelDelay();
        BufferedImage capture = this.robot.createScreenCapture();
        try {
            PipeGrid grid = PipeProcessor.getCurrentState(capture); // tries to load a grid from the current screen
            if (grid.solved()) throw new RuntimeException("grid already solved");
            return grid; // only returns when a valid grid is found
        } catch (Exception e) { // if there is not valid grid then
            if (autoNextLevel && PipeColour.closeColour(capture.getRGB(levelButtonPos.getX(), levelButtonPos.getY()), this.levelButtonColour.getFirst(), this.levelButtonColour.getSecond(), this.levelButtonColour.getThird(), this.targetRange)) {
                // if autoNextLevel is true and the next level button is there
                robot.mouseMove(levelButtonPos.getX(), levelButtonPos.getY());
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
            return nextLevel(levelButtonPos);
        }
    }

    private Point2D getLevelButton() {
        BufferedImage capture = this.robot.createScreenCapture();
        int highestTotal = -1; // finds the y with the most of the level button colour, this should be around the middle of the button
        int bestY = -1;
        for (int y = 0; y < capture.getHeight(); y++) {
            int total = 0;
            for (int x = 0; x < capture.getWidth(); x++) {
                if (PipeColour.closeColour(capture.getRGB(x,y), this.levelButtonColour.getFirst(), this.levelButtonColour.getSecond(), this.levelButtonColour.getThird(), this.targetRange)) total++;
            }
            if (total > highestTotal) {
                bestY = y;
                highestTotal = total;
            }
        }
        int lastX = -1; // finds the rightmost point of the arrow of the button
        for (int x = 0; x < capture.getWidth(); x++) {
            if (PipeColour.closeColour(capture.getRGB(x,bestY),
                    this.levelButtonColour.getFirst(),
                    this.levelButtonColour.getSecond(),
                    this.levelButtonColour.getThird(), this.targetRange)) {
                lastX = x;
            }
        }
        return new Point2D(lastX, bestY);
    }
}
