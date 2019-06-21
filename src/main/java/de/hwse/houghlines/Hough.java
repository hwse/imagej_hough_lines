package de.hwse.houghlines;

import ij.process.ImageProcessor;

import java.util.List;

public class Hough {
    private static final int WHITE = 255;
    private static final int BLACK = 0;

    public static List<Line> findLines(ImageProcessor imageProcessor, int threshhold) {
        Accumulator accumulator = fillAccumulator(imageProcessor);
        return accumulator.getAboveThreshold(threshhold);
    }

    private static Accumulator fillAccumulator(ImageProcessor imageProcessor) {
        // 180 --> cell for each angle

        Accumulator accumulator = new Accumulator(imageProcessor.getWidth(), imageProcessor.getHeight());
        // fill accumulator with "hough line" of all pixels
        for (int x = 0; x < imageProcessor.getWidth(); x++) {
            for (int y = 0; y < imageProcessor.getHeight(); y++) {

                // if pixel is over threshhold translate "hough line" to accumulator
                int pixel = imageProcessor.getPixel(x, y);
                if (pixel > 200) {
                    accumulator.countPoint(x,y);
                }
            }
        }
        return accumulator;
    }

}
