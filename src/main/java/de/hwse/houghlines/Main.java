package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
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

    private static void drawLine(ImageProcessor imageProcessor, Line line) {
        Point center = new Point(0, 0);
        Point test = line.pointAt(0);
        imageProcessor.drawLine(center.x, center.y, test.x, test.y);

        Point p0 = line.pointAt(-2000);
        Point p1 = line.pointAt(2000);
        imageProcessor.drawLine(p0.x, p0.y, p1.x, p1.y);
    }

    private static Optional<LaneDetect.Result> searchLanes(ImagePlus image) {
        image.show();
        ImageProcessor original = image.getProcessor();

        ImageProcessor graySlice = image.getProcessor().convertToByte(false);
        graySlice.findEdges();

        Optional<LaneDetect.Result> result = LaneDetect.adaptingLaneSearch(graySlice);

        if (!result.isPresent()) return result;
        LaneDetect.Result lanes = result.get();

        original.setColor(Color.GREEN);
        original.setLineWidth(3);
        for (Line line : Arrays.asList(lanes.left, lanes.right)) {
            drawLine(original, line);
        }
        /*original.setColor(Color.RED);
        original.setLineWidth(1);
        for (Line line: lanes.allFound) {
            drawLine(original, line);
        }*/
        image.repaintWindow();
        return result;
    }

    public static void main(String[] args) {
        if (args.length < 1) throw new IllegalArgumentException("pass filename as argument");
        if (!new File(args[0]).exists()) throw new IllegalArgumentException("file does not exist: " + args[0]);
        ImagePlus rawImage = IJ.openImage(args[0]);
        ImagePlus imagePlus = Util.cutImage(rawImage.getProcessor(), new Roi(0, rawImage.getHeight() / 2,
                rawImage.getWidth(), rawImage.getHeight() /2));
        rawImage.show();

        List<Roi> rois = LaneDetect.splitImage(imagePlus.getWidth(), imagePlus.getHeight());

        List<Optional<LaneDetect.Result>> results = rois.stream()
                .map(roi -> Util.cutImage(imagePlus.getProcessor(), roi))
                .map(Main::searchLanes)
                .collect(Collectors.toList());

        List<Line> finalLines = new ArrayList<>();
        for (int i = 0; i < rois.size(); i++) {
            Roi roi = rois.get(i);
            Optional<LaneDetect.Result> result = results.get(i);
            if (!result.isPresent()) continue;
            Line left = result.get().left.translate(0, rawImage.getHeight()/2 + roi.getBounds().y);
            Line right = result.get().right.translate(0, rawImage.getHeight()/2 + roi.getBounds().y);
            finalLines.add(left);
            finalLines.add(right);
        }
        finalLines.forEach(l -> drawLine(rawImage.getProcessor(), l));

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
