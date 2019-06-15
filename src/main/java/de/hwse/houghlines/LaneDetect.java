package de.hwse.houghlines;

import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LaneDetect {

    static class Result {
        public final Line left;
        public final Line right;

        Result(Line left, Line right) {
            this.left = left;
            this.right = right;
        }
    }


    private static boolean angleOkay(double angle) {
        return (20 < angle && angle < 70) || (110 < angle && angle < 160);
    }


    protected static List<Roi> splitImage(int width, int height) {
        // height = 480
        // / 2: 240 240
        // / 4: 120 120
        // / 8: 60, 60
        // / 8: 60, 60 .. or continue
        return IntStream.of(2,4,8,8)
                .map(i -> height / i)
                .mapToObj(y -> new Roi(0, y, width, y))
                .collect(Collectors.toList());
    }

    public static Optional<Result> searchLane(ImageProcessor imageProcessor, int threshold) {
        List<Line> okayCircles = Hough.findLines(imageProcessor, threshold)
                .stream().filter(l -> angleOkay(l.angle)).collect(Collectors.toList());
        if (okayCircles.isEmpty()) return Optional.empty();

        Optional<Line> left = okayCircles.stream()
                .filter(l -> 0 < l.angle && l.angle < 90)
                .min(Comparator.comparing(l -> l.angle));
        Optional<Line> right = okayCircles.stream()
                .filter(l -> 90 < l.angle && l.angle < 180)
                .max(Comparator.comparing(l -> l.angle));
        if (left.isPresent() && right.isPresent()) {
            return Optional.of(new Result(left.get(), right.get()));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Result> adaptingLaneSearch(ImageProcessor imageProcessor) {
        // try decreasing thresholds to find lanes
        return IntStream.of(300, 250, 200, 150, 100, 50)
                .mapToObj(threshold -> LaneDetect.searchLane(imageProcessor, threshold))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}
