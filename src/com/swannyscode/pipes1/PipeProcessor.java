package com.swannyscode.pipes1;

import com.swannyscode.Constants;
import com.swannyscode.util.Point2D;
import com.swannyscode.util.Tuple2;
import com.swannyscode.util.Tuple3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class PipeProcessor {

    private static PipeProcessor processor = null;
    private final int targetRed; // new Color(68,85,99).getRed;
    private final int targetBlue; // new Color(68,85,99).getBlue;
    private final int targetGreen; // new Color(68,85,99).getGreen;
    private final int otherColour; // new Color(255,255,255).getRGB();
    private final int targetRange; // 2
    private final int positive; // otherColour 255 - RGB

    private PipeProcessor(int targetColour, int targetRange, int otherColour) {
        this.otherColour = otherColour;
        this.targetRed = (targetColour >> 16) & 0xFF;
        this.targetGreen = (targetColour >> 8) & 0xFF;
        this.targetBlue = (targetColour) & 0xFF;
        this.targetRange = targetRange;
        this.positive = new Color(255 - ((otherColour >> 16) & 0xFF),
                255 - ((otherColour >> 8) & 0xFF),
                255 - ((otherColour) & 0xFF)).getRGB();
    }

    private static PipeProcessor getProcessor() {
        if (processor == null) {
            processor = new PipeProcessor(Constants.getIntConstant("pipeMatchColour"),
                    Constants.getIntConstant("pipeTargetRange"),
                    Constants.getIntConstant("pipeOtherColour"));
        }
        return processor;
    }

    public static PipeGrid getCurrentState(BufferedImage image) {
        if (image == null) throw new RuntimeException("Couldn't create screen capture");
        Tuple3<BufferedImage, Integer, int[]> results = getProcessor().reduceImage(image);
        BufferedImage i = results.getFirst();
        int[] bounds = results.getThird();
        int squareLength = results.getSecond();
        final int squareLengthVar = Constants.getIntConstant("squareLengthVariation");
        Tuple2<List<Integer>,List<Integer>> splits = getProcessor().getSplits(i);
        splits.getFirst().add(i.getWidth()); splits.getFirst().add(0, -1);
        splits.getSecond().add(i.getHeight()); splits.getSecond().add(0, -1);
        List<Tuple2<Integer, Integer>> xSquares = getSquares(splits.getFirst(), squareLength, squareLengthVar);
        List<Tuple2<Integer, Integer>> ySquares = getSquares(splits.getSecond(), squareLength, squareLengthVar);
        List<Tuple2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>> squares = Tuple2.cartesianProduct(xSquares, ySquares);
        int squareNo = 0;
        List<Pipe> pipes = new ArrayList<>();
        for (var square : squares) {
            squareNo++;
            BufferedImage squareImage = i.getSubimage(
                    square.getFirst().getFirst(),
                    square.getSecond().getFirst(),
                    square.getFirst().getSecond(),
                    square.getSecond().getSecond());
            pipes.add(getPipe(image, squareImage, new Tuple2<>(bounds[0] + square.getFirst().getFirst(), bounds[1] + square.getSecond().getFirst())));
        }
        return new PipeGrid((int) Math.sqrt(squareNo), pipes);
    }

    private static Pipe getPipe(BufferedImage image, BufferedImage square, Tuple2<Integer, Integer> pos) {
        int midX = Math.floorDiv(square.getWidth(), 2);
        int midY = Math.floorDiv(square.getHeight(), 2);
        Point2D center = new Point2D(midX + pos.getFirst(), midY + pos.getSecond());
        int otherColour = getProcessor().otherColour;
        boolean northConnection = square.getRGB(midX, 1) == otherColour;
        boolean eastConnection = square.getRGB(square.getWidth()-2, midY) == otherColour;
        boolean southConnection = square.getRGB(midX, square.getHeight()-2) == otherColour;
        boolean westConnection = square.getRGB(1, midY) == otherColour;
        List<Boolean> connections = Arrays.asList(northConnection, eastConnection, southConnection, westConnection);
        int connectionNo = 0;
        for (boolean connection : connections) {
            if (connection) connectionNo++;
        }
        switch (connectionNo) {
            case 0: return Pipe.nullPipe;
            case 1: return oneConnection(image, square, center, connections);
            case 2:
            case 3:
                return simpleConnection(center, connections);
            case 4: return fourConnection(square, new Point2D(midX, midY), center);
        }
        throw new RuntimeException("Pipe can't be connected to more then 4 other pipes");
    }

    private static Pipe oneConnection(BufferedImage image, BufferedImage square, Point2D center, List<Boolean> connections) {
        int pos = 0;
        int neg = 0;
        for (int x = 0; x < square.getWidth(); x++) {
            for (int y = 0; y < square.getHeight(); y++) {
                if (square.getRGB(x,y) == getProcessor().otherColour) pos++;
                else neg++;
            }
        }
        double percentage = (pos * 100.0) / (pos + neg);
        PipeColour colour = PipeColour.fromRGB(image.getRGB(center.getX(), center.getY()));
        PipeDirection direction = null;
        for (int i = 0; i < 4; i++) {
            if (connections.get(i)) direction = PipeDirection.fromIndex(i);
        }
        if (direction == null) throw new RuntimeException("Pipes with one connection can't have no connections");
        if (percentage > 35) {
            return new Sink(center, direction, colour);
        } else {
            return new Source(center, direction, colour);
        }
    }

    private static Pipe simpleConnection(Point2D center, List<Boolean> connections) {
        List<PipeDirection> link = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (connections.get(i)) link.add(PipeDirection.fromIndex(i));
        }
        if (link.size() == 3) {
            return new Connector(center, new ArrayList<>(Collections.singleton(link)), 4); // _|_
        } else {
            if (link.get(0) == link.get(1).opposite()) {
                return new Connector(center, new ArrayList<>(Collections.singleton(link)), 2); // -
            } else {
                return new Connector(center, new ArrayList<>(Collections.singleton(link)), 4); // ¬
            }
        }
    }

    private static Pipe fourConnection(BufferedImage square, Point2D squareCenter, Point2D center) {
        boolean cross = true;
        for (int y = squareCenter.getY() - 1; y <= squareCenter.getY() + 1; y++) {
            for (int x = squareCenter.getX() - 1; x <= squareCenter.getX() + 1; x++) {
                if (square.getRGB(x,y) != getProcessor().otherColour) cross = false;
            }
        }
        if (cross) { // +
            boolean allBlank = true;
            for (int x = 0; x < square.getWidth(); x++) {
                for (int y = 0; y < square.getHeight(); y++) {
                    if (square.getRGB(x,y) != getProcessor().otherColour) allBlank = false;
                }
            }
            if (allBlank) throw new RuntimeException("pipe cannot be all blank"); // prevents blank squared being treated as crosses, it throws an error instead
            List<PipeDirection> link1 = new ArrayList<>(Arrays.asList(PipeDirection.NORTH, PipeDirection.SOUTH));
            List<PipeDirection> link2 = new ArrayList<>(Arrays.asList(PipeDirection.EAST, PipeDirection.WEST));
            return new Connector(center, Arrays.asList(link1, link2), -1);
        } else { // its two corner bends
            int halfWidth = Math.floorDiv(square.getWidth(), 2); // splits the square into 4 corners and counts how many pixels are pipe colour
            int halfHeight = Math.floorDiv(square.getHeight(), 2);
            int topLeft = countPixels(square.getSubimage(0,0, halfWidth, halfHeight), getProcessor().otherColour);
            int topRight = countPixels(square.getSubimage(halfWidth -1, 0, square.getWidth() - halfHeight, halfHeight), getProcessor().otherColour);
            int bottomLeft = countPixels(square.getSubimage(0, halfHeight -1, halfWidth, square.getHeight() - halfHeight), getProcessor().otherColour);
            int bottomRight = countPixels(square.getSubimage(halfWidth -1, halfHeight -1, square.getWidth() - halfWidth, square.getHeight() - halfHeight), getProcessor().otherColour);
            if (topLeft > topRight && topLeft > bottomLeft) {
                if (bottomRight > topRight && bottomRight > bottomLeft) {
                    List<PipeDirection> link1 = new ArrayList<>(Arrays.asList(PipeDirection.NORTH, PipeDirection.WEST));
                    List<PipeDirection> link2 = new ArrayList<>(Arrays.asList(PipeDirection.EAST, PipeDirection.SOUTH));
                    return new Connector(center, Arrays.asList(link1, link2), 2);
                } else throw new RuntimeException("shouldn't get here");
            } else if (topRight > topLeft && topRight > bottomRight) {
                if (bottomLeft > topLeft && bottomLeft > bottomRight) {
                    List<PipeDirection> link1 = new ArrayList<>(Arrays.asList(PipeDirection.NORTH, PipeDirection.EAST));
                    List<PipeDirection> link2 = new ArrayList<>(Arrays.asList(PipeDirection.SOUTH, PipeDirection.WEST));
                    return new Connector(center, Arrays.asList(link1, link2), 2);
                } else throw new RuntimeException("shouldn't get here");
            } else throw new RuntimeException("shouldn't get here");
        }
    }

    private static int countPixels(BufferedImage image, int colour) {
        int total = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x,y) == colour) total++;
            }
        }
        return total;
    }

    private Tuple3<BufferedImage, Integer, int[]> reduceImage(BufferedImage image) {
        BufferedImage i = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        int maxX = 0;
        int maxY = 0;
        int minX = image.getWidth();
        int minY = -1;
        int squareLength = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            int currentSquareLength = 0;
            for (int x = 0; x < image.getWidth(); x++) {
                if (PipeColour.closeColour(image.getRGB(x,y), targetRed, targetGreen, targetBlue, targetRange)) {
                    i.setRGB(x,y,positive);
                    if (minY == -1) minY = y;
                    maxY = y;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    currentSquareLength++;
                } else {
                    i.setRGB(x,y, otherColour);
                    if (squareLength < currentSquareLength) squareLength = currentSquareLength;
                    currentSquareLength = 0;
                }
            }
            if (squareLength < currentSquareLength) squareLength = currentSquareLength;
        }
        i = i.getSubimage(minX, minY, maxX - minX, maxY - minY);
        int[] bounds = {minX, minY, maxX, maxY};
        return new Tuple3<>(i, squareLength, bounds);
    }

    private Tuple2<List<Integer>,List<Integer>> getSplits(BufferedImage i) {
        List<Integer> xSplits = new LinkedList<>();
        for (int x = 0; x < i.getWidth(); x++) {
            int weight = 0;
            for (int y = 0; y < i.getHeight(); y++) {
                if (i.getRGB(x,y) == otherColour) weight--;
                else weight++;
            }
            if (weight / (float) i.getHeight() < 0.1) xSplits.add(x);
        }
        List<Integer> ySplits = new LinkedList<>();
        for (int y = 0; y < i.getHeight(); y++) {
            int weight = 0;
            for (int x = 0; x < i.getWidth(); x++) {
                if (i.getRGB(x,y) == otherColour) weight--;
                else weight++;
            }
            if (weight / (float) i.getHeight() < -0.1) ySplits.add(y);
        }
        return new Tuple2<>(xSplits, ySplits);
    }

    private static List<Tuple2<Integer, Integer>> getSquares(List<Integer> splits, int squareLength, int squareVar) {
        double avgSquareLen = (double) splits.get(splits.size()-1) / Math.floorDiv(splits.get(splits.size()-1), squareLength);
        splits.removeIf(x -> (x % avgSquareLen) > squareVar && (x % avgSquareLen) < squareLength - squareVar);
        ArrayList<Tuple2<Integer, Integer>> compressedSplits = new ArrayList<>();
        int lowerBound = splits.get(0);
        int upperBound = lowerBound;
        for (int i = 1; i < splits.size(); i++) {
            if (splits.get(i) == upperBound + 1) upperBound++;
            else {
                compressedSplits.add(new Tuple2<>(lowerBound, upperBound));
                lowerBound = splits.get(i);
                upperBound = lowerBound;
            }
        }
        compressedSplits.add(new Tuple2<>(lowerBound, upperBound));
        List<Tuple2<Integer, Integer>> results = new ArrayList<>();
        for (int i = 1; i < compressedSplits.size(); i++) {
            int startingX = compressedSplits.get(i-1).getSecond() + 1;
            results.add(new Tuple2<>(startingX, compressedSplits.get(i).getFirst() - startingX));
        }
        return results;
    }
}
