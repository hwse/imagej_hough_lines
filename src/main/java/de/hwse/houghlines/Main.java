package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {



    private static Optional<LaneDetect.Result> searchLanes(ImagePlus image, LaneDetect.Result previous) {
        image.show();
        ImageProcessor original = image.getProcessor();

        // ImageProcessor graySlice = image.getProcessor().convertToByte(false);
        //graySlice.findEdges();
        //graySlice.threshold(200);

        // converting to binary image can also be used
        ImageProcessor binary = image.getProcessor().convertToByte(false);
        binary.threshold(200);

        ImagePlus binaryImage = new ImagePlus("binary", binary);
        binaryImage.show();

        //ImagePlus grayImage = new ImagePlus("gray", gray);
        //grayImage.show();

        Optional<LaneDetect.Result> result = LaneDetect.adaptingLaneSearch(binary, previous);

        if (!result.isPresent()) return result;
        LaneDetect.Result lanes = result.get();

        original.setColor(Color.RED);
        original.setLineWidth(1);
        for (Line line: lanes.allFound) {
            Util.drawLine(original, line);
        }
        original.setColor(Color.GREEN);
        original.setLineWidth(3);
        for (Line line : Arrays.asList(lanes.left, lanes.right)) {
            Util.drawLine(original, line);
        }
        image.repaintWindow();
        return result;
    }

    public static void main(String[] args) {
        if (args.length < 1) throw new IllegalArgumentException("pass filename as argument");
        if (!new File(args[0]).exists()) throw new IllegalArgumentException("file does not exist: " + args[0]);
        ImagePlus rawImage = IJ.openImage(args[0]);
        int halftHeight = rawImage.getHeight() / 2;
        ImagePlus imagePlus = Util.cutImage(rawImage.getProcessor(), new Roi(0, halftHeight,
                rawImage.getWidth(), halftHeight));
        rawImage.show();

        List<Roi> rois = LaneDetect.splitImage(imagePlus.getWidth(), imagePlus.getHeight());

        List<Optional<LaneDetect.Result>> results = new ArrayList<>();
        LaneDetect.Result lastResult = null;
        for (Roi roi: rois) {
            ImagePlus cutImage = Util.cutImage(imagePlus.getProcessor(), roi);
            Optional<LaneDetect.Result> result = searchLanes(cutImage, lastResult);
            lastResult = result.orElse(null);
            results.add(result);
        }

        List<Pair<Point, Point>> finalLines = new ArrayList<>();
        for (int i = 0; i < rois.size(); i++) {
            Roi roi = rois.get(i);
            Optional<LaneDetect.Result> result = results.get(i);
            if (!result.isPresent()) continue;

            Line left = result.get().left.translate(0, roi.getBounds().y);
            Line right = result.get().right.translate(0, roi.getBounds().y);

            Point l0 = left.positionAtY(roi.getBounds().y).translate(0, halftHeight).roundToPoint();
            Point l1 = left.positionAtY(roi.getBounds().y + roi.getBounds().height).translate(0, halftHeight).roundToPoint();
            Point r0 = right.positionAtY(roi.getBounds().y).translate(0, halftHeight).roundToPoint();
            Point r1 = right.positionAtY(roi.getBounds().y + roi.getBounds().height).translate(0, halftHeight).roundToPoint();
            finalLines.add(Pair.of(l0, l1));
            finalLines.add(Pair.of(r0, r1));
        }
        ImageProcessor raw = rawImage.getProcessor();
        raw.setColor(Color.GREEN);
        raw.setLineWidth(10);
        /*Pair<Pair<Point, Point>, Pair<Point, Point>> iter = finalLines.get(0);
        for (Pair<Pair<Point, Point>, Pair<Point, Point>> l: finalLines) {
            drawPointLine(raw, iter.getFirst().getSecond(), l.getFirst().getSecond());
            drawPointLine(raw, iter.getSecond().getFirst(), l.getSecond().getSecond());
            iter = l;
        }*/
        finalLines.forEach(l -> {
            System.out.println(l);
            Util.drawPointLine(raw, l.getFirst(), l.getSecond());
        });

        results.forEach(res -> {
            if (!res.isPresent()) {
                System.out.println("not present");
            } else {
                LaneDetect.Result result = res.get();
                System.out.println("left = " + result.left + ", right = " + result.right);
            }
        });
    }

}
