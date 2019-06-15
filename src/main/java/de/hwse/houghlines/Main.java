package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Main {

    private static void searchLanes(ImagePlus image) {
        image.show();
        ImageProcessor original = image.getProcessor();

        ImageProcessor graySlice = image.getProcessor().convertToByte(false);
        ImagePlus grayImage = new ImagePlus("", graySlice);
        graySlice.findEdges();

        Optional<LaneDetect.Result> result = LaneDetect.adaptingLaneSearch(graySlice);

        if (!result.isPresent()) return;
        LaneDetect.Result lanes = result.get();

        original.setColor(Color.GREEN);
        original.setLineWidth(3);
        for (Line line : Arrays.asList(lanes.left, lanes.right)) {
            Point center = new Point(0, 0);
            Point test = line.pointAt(0);
            original.drawLine(center.x, center.y, test.x, test.y);

            Point p0 = line.pointAt(-2000);
            Point p1 = line.pointAt(2000);
            original.drawLine(p0.x, p0.y, p1.x, p1.y);
        }
        image.repaintWindow();
    }

    public static void main(String[] args) {
        if (args.length < 1) throw new IllegalArgumentException("pass filename as argument");
        if (!new File(args[0]).exists()) throw new IllegalArgumentException("file does not exist: " + args[0]);
        ImagePlus rawImage = IJ.openImage(args[0]);
        ImagePlus imagePlus = Util.cutImage(rawImage.getProcessor(), new Roi(0, rawImage.getHeight() / 2,
                rawImage.getWidth(), rawImage.getHeight() /2));
        imagePlus.show();

        List<Roi> rois = LaneDetect.splitImage(imagePlus.getWidth(), imagePlus.getHeight());
        for (Roi roi: rois) {
            ImagePlus sliceImage = Util.cutImage(imagePlus.getProcessor(), roi);
            searchLanes(sliceImage);
        }
    }

}
