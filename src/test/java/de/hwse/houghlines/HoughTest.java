package de.hwse.houghlines;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HoughTest {

    @Test
    void testAngleToRad() {
        assertEquals(0, Util.angleToRad(0));
        assertEquals(Math.PI, Util.angleToRad(180));
        assertEquals(-Math.PI, Util.angleToRad(-180));
    }

    @Test
    void testRadToAngle() {
        assertEquals(0, Util.radToAngle(0));
        assertEquals(180, Util.radToAngle(Math.PI));
        assertEquals(-180, Util.radToAngle(-Math.PI));
    }

    @Test
    void testLineFindTest() {
        ImagePlus imagePlus = IJ.createImage("test", "8-bit-black", 4, 4, 0);
        ImageProcessor imageProcessor = imagePlus.getProcessor();
        for (int y = 0; y < 4; y++)
            imageProcessor.putPixel(2, y, 255);
        List<Line> lines = Hough.findLines(imageProcessor, 3);
        assertTrue(lines.contains(new Line(0, 2.0)));
    }

}