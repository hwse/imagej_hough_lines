package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Main {

    private static ImagePlus cutImage(ImagePlus imagePlus, Roi roi) {
        ImageProcessor imageProcessor = imagePlus.getProcessor();
        imageProcessor.setRoi(roi);
        imageProcessor = imageProcessor.crop();
        return new ImagePlus(imagePlus.getTitle(), imageProcessor);
    }


    public static void main(String[] args) {
        if (args.length < 1) throw new IllegalArgumentException("pass filename as argument");
        if (!new File(args[0]).exists()) throw new IllegalArgumentException("file does not exist: " + args[0]);
        ImagePlus rawImage = IJ.openImage(args[0]);
        ImagePlus imagePlus = cutImage(rawImage, new Roi(0, rawImage.getHeight() / 2,
                rawImage.getWidth(), rawImage.getHeight() /2));
        imagePlus.show();

        // find edges
        ImageProcessor imageProcessor = imagePlus.getProcessor().convertToByte(false);
        imageProcessor.findEdges();

        // try decreasing thresholds to find lanes
        Optional<LaneDetect.Result> result = IntStream.of(200, 150, 100, 50)
                .mapToObj(threshold -> LaneDetect.searchLane(imageProcessor, threshold))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        LaneDetect.Result lanes = result.orElseThrow(() -> new RuntimeException("could not find any lanes"));

        ImageProcessor original = imagePlus.getProcessor();
        original.setColor(Color.GREEN);
        original.setLineWidth(3);
        for(Line line: Arrays.asList(lanes.left, lanes.right)) {
            Point center = new Point(0 ,0);
            Point test = line.pointAt(0);
            original.drawLine(center.x, center.y, test.x, test.y);

            Point p0 = line.pointAt(-2000);
            Point p1 = line.pointAt(2000);
            original.drawLine(p0.x, p0.y, p1.x, p1.y);
        }
        imagePlus.repaintWindow();
    }

}
