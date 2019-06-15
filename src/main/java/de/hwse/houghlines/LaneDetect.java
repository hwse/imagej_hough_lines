package de.hwse.houghlines;

import ij.process.ImageProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

}
