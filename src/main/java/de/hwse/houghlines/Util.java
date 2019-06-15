package de.hwse.houghlines;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

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

}
