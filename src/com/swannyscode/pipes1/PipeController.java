package com.swannyscode.pipes1;

import com.swannyscode.Constants;
import com.swannyscode.util.Tuple3;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PipeController extends Thread {

    private final BlockingQueue<Tuple3<AtomicBoolean, Pipe, Integer>> instructions;
    private final Map<Pipe, Integer> currentRotations = new HashMap<>();
    private final Robot robot;
    private final int delay = Constants.getIntConstant("pipeRotationDelay");

    public PipeController(BlockingQueue<Tuple3<AtomicBoolean, Pipe, Integer>> instructions,
                          Robot r) {
        this.instructions = instructions;
        this.robot = r;
    }

    public void run() {
        boolean running = true;
        while (running) {
            Tuple3<AtomicBoolean, Pipe, Integer> instruction = null;
            try {
                instruction = instructions.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (instruction == null) throw new RuntimeException("Can't have a null instruction");
            if (Pipe.nullPipe.equals(instruction.getSecond())) running = false; // if terminating instruction
            else if (instruction.getFirst().get()) { // process instruction as it's valid
                Pipe pipe = instruction.getSecond();
                int targetRotation = instruction.getThird();
                if (targetRotation >= pipe.getMaxRotation()) throw new RuntimeException("Can't rotate that much");
                int currentRotation = this.currentRotations.getOrDefault(pipe, 0);
                if (currentRotation != targetRotation) { // while target != current, rotates the pipe
                    while (currentRotation != targetRotation) {
                        robot.mouseMove(pipe.getCenter().getX(), pipe.getCenter().getY()); // moves mouse to pipe
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // clicks
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        currentRotation = (currentRotation + 1) % pipe.getMaxRotation();
                        try {
                            TimeUnit.MILLISECONDS.sleep(this.delay); // waits for animation to finish
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
