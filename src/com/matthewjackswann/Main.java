package com.matthewjackswann;

import com.matthewjackswann.pipes1.PipeGame;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        RestrictedRobot r = RestrictedRobot.getRestrictedRobot();
        System.out.println(TimeUnit.SECONDS.convert(
                new PipeGame(r).play(),
                TimeUnit.MILLISECONDS) + " seconds taken solving grids (not including rotating pipes and changing levels)");
    }
}
