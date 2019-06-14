package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    private static ImagePlus cutImage(ImagePlus imagePlus, Roi roi) {
        ImageProcessor imageProcessor = imagePlus.getProcessor();
        imageProcessor.setRoi(roi);
        imageProcessor = imageProcessor.crop();
        return new ImagePlus(imagePlus.getTitle(), imageProcessor);
    }

    private static boolean angleOkay(double angle) {
        return (20 < angle && angle < 70) || (110 < angle && angle < 160);
    }

    public static void main(String[] args) {
        if (args.length < 1) throw new IllegalArgumentException("pass filename as argument");
        ImagePlus rawImage = IJ.openImage(args[0]);
        ImagePlus imagePlus = cutImage(rawImage, new Roi(0, rawImage.getHeight() / 2,
                rawImage.getWidth(), rawImage.getHeight() /2));
        imagePlus.show();

        // find edges
        ImageProcessor imageProcessor = imagePlus.getProcessor().convertToByte(false);
        imageProcessor.findEdges();

        for (int threshhold = 200; threshhold >= 0; threshhold -= 50) {
            List<Line> okayCircles = Hough.findLines(imageProcessor, threshhold)
                    .stream().filter(l -> angleOkay(l.angle)).collect(Collectors.toList());
            if (okayCircles.isEmpty()) continue;
            Optional<Line> left = okayCircles.stream()
                    .filter(l -> 0 < l.angle && l.angle < 90)
                    .min(Comparator.comparing(l -> l.angle));
            Optional<Line> right = okayCircles.stream()
                    .filter(l -> 90 < l.angle && l.angle < 180)
                    .max(Comparator.comparing(l -> l.angle));
            if (!left.isPresent() || !right.isPresent()) continue;

            ImageProcessor original = imagePlus.getProcessor();
            original.setColor(Color.GREEN);
            original.setLineWidth(3);
            for(Line line: Arrays.asList(left.get(), right.get())) {
                Point p0 = line.pointAt(-2000);
                Point p1 = line.pointAt(2000);
                original.drawLine(p0.x, p0.y, p1.x, p1.y);
            }
            imagePlus.repaintWindow();
            break;
        }
    }

}
