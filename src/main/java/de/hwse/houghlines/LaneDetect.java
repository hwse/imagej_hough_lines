package de.hwse.houghlines;

import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LaneDetect {
    private static final Logger LOGGER = Logger.getLogger(LaneDetect.class.getSimpleName());

    static class Result {
        public final Line left;
        public final Line right;
        public final List<Line> allFound;

        Result(Line left, Line right, List<Line> allFound) {
            this.left = left;
            this.right = right;
            this.allFound = allFound;
        }
    }

    static class PartialResult {
        public final Line lane;
        public final List<Line> allFound;

        public PartialResult(Line lane, List<Line> allFound) {
            this.lane = lane;
            this.allFound = allFound;
        }
    }

    private static final boolean angleOkay(Line line) {
        double angle = line.sanitizeAngle().angle;
        return angle < 80.0 || angle > 100.0;
    }

    protected static List<Roi> splitImage(int width, int height) {
        // height = 480
        // / 2: 240 240
        // / 4: 120 120
        // / 8: 60, 60
        // / 8: 60, 60 .. or continue
        return IntStream.of(2,4,8,16)
                .map(i -> height / i)
                .mapToObj(y -> new Roi(0, y, width, y))
                .collect(Collectors.toList());
    }

    private static Predicate<Line> createPreviousFilter(Result previous, double width, double height, boolean right) {
        if (previous != null) {
            double maxPreviousDiff = 100.0;
            double maxAngleDiff = 90.0;

            Line previousLine = right ? previous.right : previous.left;
            return line -> Math.abs(previousLine.xAt(0) - line.xAt(height)) < maxPreviousDiff &&
                    previousLine.angleDiff(line) < maxAngleDiff;
        } else {
            if (right) {
                return line -> line.xAt(height) >= width * 0.5 && line.xAt(0) >= width * 0.25;
            } else {
                return line -> line.xAt(height) < width * 0.5 && line.xAt(0) < width * 0.75;
            }
        }
    }

    public static Optional<PartialResult> searchLane(boolean right, ImageProcessor imageProcessor, int threshold, Result previous) {
        LOGGER.info(() -> "running with threshold " + threshold);
        List<Line> okayCircles = Hough.findLines(imageProcessor, threshold);
        if (okayCircles.isEmpty()) return Optional.empty();

        //Util.debugLines(imageProcessor, okayCircles);

        double width = imageProcessor.getWidth();
        double height = imageProcessor.getHeight();


        // filter that checks lines start where previous line approximately ended
        Predicate<Line> filter = createPreviousFilter(previous, width, height, right);

        final Line leftTarget = new Line(20, 1.0);
        final Line rigthTarget = new Line(-20, 1.0);
        // select left and right lang by searching minimum of this comparator
        Comparator<Line> leftComparator = previous != null ?
                Comparator.comparing(l -> Math.abs(previous.left.xAt(0)-l.xAt(height))):
                Comparator.comparing(leftTarget::angleDiff);
        Comparator<Line> rightComparator = previous != null ?
                Comparator.comparing(l -> Math.abs(previous.right.xAt(0)-l.xAt(height))):
                Comparator.comparing(rigthTarget::angleDiff); //l.xAt(height)

        Optional<Line> lane = okayCircles.stream()
                .map(Line::sanitizeAngle)
                .filter(LaneDetect::angleOkay)
                //.filter(l -> 0 < l.angle && l.angle < 90)
                //.filter(l -> l.xAt(height) < width * 3 / 4)
                .filter(filter)
                //.max(Comparator.comparing(l -> l.xAt(height)));
                .min(right ? rightComparator : leftComparator);
        return lane.map(l -> new LaneDetect.PartialResult(l, okayCircles));
    }

    public static Optional<Result> searchLane(ImageProcessor imageProcessor, int threshold, Result previous) {
        LOGGER.info(() -> "running with threshold " + threshold);
        List<Line> okayCircles = Hough.findLines(imageProcessor, threshold);
        if (okayCircles.isEmpty()) return Optional.empty();

        //Util.debugLines(imageProcessor, okayCircles);

        double width = imageProcessor.getWidth();
        double height = imageProcessor.getHeight();


        // filter that checks lines start where previous line approximately ended
        Predicate<Line> previousLeftFilter = previous != null ?
                createPreviousFilter(previous, width, height, false) :
                line -> line.xAt(height) < width * 0.5 && line.xAt(0) < width * 0.75;
        Predicate<Line> previousRightFilter = previous != null ?
                createPreviousFilter(previous, width, height, true) :
                line -> line.xAt(height) >= width * 0.5 && line.xAt(0) >= width * 0.25;

        final Line leftTarget = new Line(20, 1.0);
        final Line rigthTarget = new Line(-20, 1.0);
        // select left and right lang by searching minimum of this comparator
        Comparator<Line> leftComparator = previous != null ?
                Comparator.comparing(l -> Math.abs(previous.left.xAt(0)-l.xAt(height))):
                Comparator.comparing(leftTarget::angleDiff);
        Comparator<Line> rightComparator = previous != null ?
                Comparator.comparing(l -> Math.abs(previous.right.xAt(0)-l.xAt(height))):
                Comparator.comparing(rigthTarget::angleDiff); //l.xAt(height)

        Optional<Line> left = okayCircles.stream()
                .map(Line::sanitizeAngle)
                .filter(LaneDetect::angleOkay)
                //.filter(l -> 0 < l.angle && l.angle < 90)
                //.filter(l -> l.xAt(height) < width * 3 / 4)
                .filter(previousLeftFilter)
                //.max(Comparator.comparing(l -> l.xAt(height)));
                .min(leftComparator);
        Optional<Line> right = okayCircles.stream()
                .map(Line::sanitizeAngle)
                .filter(LaneDetect::angleOkay)
                //.filter(l -> 90 < l.angle && l.angle < 180)
                //.filter(l -> l.xAt(height) >= width / 4)
                .filter(previousRightFilter)
                //.min(Comparator.comparing(l -> l.xAt(height)));
                .min(rightComparator);
        if (left.isPresent() && right.isPresent()) {
            // prevent that both lanes are the same
            double laneLength = 30.0;
            if (Math.abs(left.get().xAt(height) - right.get().xAt(height)) < laneLength ||
                Math.abs(left.get().xAt(0) - right.get().xAt(0)) < laneLength) {
                LOGGER.info(() -> "left and right lane are too close to each other");
                return Optional.empty();
            }
            return Optional.of(new Result(left.get(), right.get(), okayCircles));
        } else {
            LOGGER.info(() -> "no left or right lane");
            return Optional.empty();
        }
    }

    public static Optional<Result> adaptingLaneSearch(ImageProcessor imageProcessor, Result previous) {
        // try decreasing thresholds to find lanes
        int startThreshold = imageProcessor.getHeight() * 3 / 4;
        /*return IntStream.iterate(startThreshold, i -> i - 20)
                .filter(i -> i > 0)
                .limit(10)
                .mapToObj(threshold -> LaneDetect.searchLane(imageProcessor, threshold, previous))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst(); */
        Optional<PartialResult> left = IntStream.iterate(startThreshold, i -> i - 20)
                .filter(i -> i > 0)
                .limit(10)
                .mapToObj(threshold -> LaneDetect.searchLane(false, imageProcessor, threshold, previous))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        Optional<PartialResult> right = IntStream.iterate(startThreshold, i -> i - 20)
                .filter(i -> i > 0)
                .limit(10)
                .mapToObj(threshold -> LaneDetect.searchLane(true, imageProcessor, threshold, previous))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> left.map(l -> l.lane != p.lane).orElse(true))
                .findFirst();
        if (left.isPresent() && right.isPresent()) {
            PartialResult leftResult = left.get();
            PartialResult rightResult = right.get();
            List<Line> lines = new ArrayList<>();
            lines.addAll(leftResult.allFound);
            lines.addAll(rightResult.allFound);
            return Optional.of(new Result(leftResult.lane, rightResult.lane, lines));
        } else {
            return Optional.empty();
        }
    }

}
