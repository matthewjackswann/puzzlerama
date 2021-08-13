package com.matthewjackswann.pipes1;

import com.matthewjackswann.util.BiMap;
import com.matthewjackswann.util.Tuple3;

import java.util.Collections;
import java.util.Comparator;

public enum PipeColour {
    BLUE,
    GREEN,
    RED,
    MAGENTA,
    ORANGE,
    CYAN,
    CLEAR,
    INVALID;

    public PipeColour add(PipeColour colour) {
        if (this == colour) return colour;
        if (this == CLEAR) return colour;
        if (colour == CLEAR) return this;
        if (this == BLUE) {
            switch (colour) {
                case RED:
                case MAGENTA:
                    return MAGENTA;
                case GREEN:
                case CYAN:
                    return CYAN;
            }
        } else if (this == GREEN) {
            switch (colour) {
                case RED:
                case ORANGE:
                    return ORANGE;
                case BLUE:
                case CYAN:
                    return CYAN;
            }
        } else if (this == RED) {
            switch (colour) {
                case GREEN:
                case ORANGE:
                    return ORANGE;
                case BLUE:
                case MAGENTA:
                    return MAGENTA;
            }
        }
        return INVALID;
    }

    private static final BiMap<PipeColour, Tuple3<Integer, Integer, Integer>> colourMapping = new BiMap<>() {{
        put(BLUE, new Tuple3<>(72, 140, 235));
        put(GREEN, new Tuple3<>(141, 220, 65));
        put(RED, new Tuple3<>(230, 82, 103));
        put(MAGENTA, new Tuple3<>(181, 99, 223));
        put(ORANGE, new Tuple3<>(232, 179,81));
        put(CYAN, new Tuple3<>(0, 200, 198));
    }};

    public static PipeColour fromRGB(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb) & 0xFF;
        return colourMapping.getKey(Collections.min(colourMapping.getValues(), new Comparator<>() {
            @Override
            public int compare(Tuple3<Integer, Integer, Integer> t0, Tuple3<Integer, Integer, Integer> t1) {
                return distance(t0.getFirst(), t0.getSecond(), t0.getThird()) - distance(t1.getFirst(), t1.getSecond(), t1.getThird());
            }

            private int distance(int r1, int g1, int b1) {
                return (int) Math.floor(Math.sqrt(((r1 - r) * (r1 - r)) + ((g1 - g) * (g1 - g)) + ((b1 - b) * (b1 - b))));
            }
        }));
    }

    public static boolean closeColour(int rgb, int targetRed, int targetGreen, int targetBlue, int targetRange) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb) & 0xFF;
        return targetRed - targetRange <= r && r <= targetRed + targetRange &&
                targetGreen - targetRange <= g && g <= targetGreen + targetRange &&
                targetBlue - targetRange <= b && b <= targetBlue + targetRange;
    }
}
