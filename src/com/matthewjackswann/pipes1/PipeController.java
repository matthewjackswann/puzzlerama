package com.matthewjackswann.pipes1;

import com.matthewjackswann.Constants;
import com.matthewjackswann.util.Point2D;
import com.matthewjackswann.util.Tuple2;
import com.matthewjackswann.util.Tuple3;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PipeController extends Thread {

    private final BlockingQueue<Tuple3<Boolean[], Point2D, Integer>> instructions;
    private final Map<Point2D, Tuple2<Point2D, Integer>> pipeInfo;
    private final Map<Point2D, Integer> currentRotations = new HashMap<>();
    private final Robot robot;
    private final int delay = Constants.getIntConstant("pipeRotationDelay");

    public PipeController(BlockingQueue<Tuple3<Boolean[], Point2D, Integer>> instructions,
                          Map<Point2D, Tuple2<Point2D, Integer>> pipeInfo,
                          Robot r) {
        this.instructions = instructions;
        this.pipeInfo = pipeInfo;
        this.robot = r;
    }

    public void run() {
        boolean running = true;
        while (running) {
            Tuple3<Boolean[], Point2D, Integer> instruction = null;
            try {
                instruction = instructions.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (instruction == null) throw new RuntimeException("Can't have a null instruction");
            if (new Point2D(-1,-1).equals(instruction.getSecond())) running = false;
            else if (instruction.getFirst()[0]) { // process instruction as it's valid
                Point2D pipe = instruction.getSecond();
                int targetRotation = instruction.getThird();
                int currentRotation = this.currentRotations.getOrDefault(pipe, 0);
                if (currentRotation != targetRotation) {
                    int maxRotation = this.pipeInfo.get(pipe).getSecond();
                    if (targetRotation >= maxRotation) throw new RuntimeException("Can't rotate that much");
                    Point2D pipeCenter = this.pipeInfo.get(pipe).getFirst();
                    while (currentRotation != targetRotation) {
                        robot.mouseMove(pipeCenter.getX(), pipeCenter.getY());
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        currentRotation = (currentRotation + 1) % maxRotation;
                        try {
                            TimeUnit.MILLISECONDS.sleep(this.delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    this.currentRotations.put(pipe, currentRotation);
                }
            }
        }
    }
}
