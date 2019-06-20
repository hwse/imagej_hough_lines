package de.hwse.houghlines;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class Util {
    protected static double angleToRad(double angle) {
        return angle * 2 * Math.PI / 360;
    }

    protected static double radToAngle(double rad) {
        return rad * 360 / (2*Math.PI);
    }

    public static ImagePlus cutImage(ImageProcessor imageProcessor, Roi roi) {
        imageProcessor.setRoi(roi);
        imageProcessor = imageProcessor.crop();
        return new ImagePlus("cut image", imageProcessor);
    }

    public static void drawLine(ImageProcessor imageProcessor, Line line) {
        //Point center = new Point(0, 0);
        //Point test = line.pointAt(0);
        //imageProcessor.drawLine(center.x, center.y, test.x, test.y);

        Point p0 = line.pointAt(-2000);
        Point p1 = line.pointAt(2000);
        drawPointLine(imageProcessor, p0, p1);
    }

    public static void drawPointLine(ImageProcessor imageProcessor, Point p0, Point p1) {
        imageProcessor.drawLine(p0.x, p0.y, p1.x, p1.y);
    }

    public static void debugLines(ImageProcessor imageProcessor, List<Line> lines) {
        ImagePlus image = new ImagePlus("debug", imageProcessor);
        ImageProcessor processor = image.getProcessor();
        processor.setColor(Color.GREEN);
        for (Line l: lines) {
            drawLine(imageProcessor, l);
        }
        image.show();
    }

}
