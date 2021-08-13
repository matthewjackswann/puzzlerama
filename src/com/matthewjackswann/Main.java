package com.matthewjackswann;

import com.matthewjackswann.pipes1.PipeGame;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Pattern isNumber = Pattern.compile("^\\d{1,3}$");
        if (args.length == 3) {
            if ("pipes1".equals(args[0])) {
                if (isNumber.matcher(args[1]).find()) {
                    if (Pattern.compile("^[ft]$").matcher(args[2]).find()) {
                        TimeUnit.SECONDS.sleep(1);
                        RestrictedRobot r = RestrictedRobot.getRestrictedRobot();
                        System.out.println(TimeUnit.SECONDS.convert(
                                new PipeGame(Integer.parseInt(args[1]), r, true).play(),
                                TimeUnit.MILLISECONDS) + " seconds taken solving grids (not including rotating pipes and changing levels)");
                    } else {
                        System.out.println("Unexpected argument in pipes1: \"" + args[2] + "\" expected \"t\" or \"f\"");
                    }
                } else {
                    System.out.println("Unexpected argument in pipes1: \"" + args[1] + "\" expected a number < 1000");
                }
            }
        }
        System.out.println("Unexpected arguments\nValid patterns are:");
        System.out.println("pipes1 <number of puzzles to solve> <auto start next level<t/f>>");
    }
}
