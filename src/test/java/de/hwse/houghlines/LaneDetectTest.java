package de.hwse.houghlines;

import ij.gui.Roi;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaneDetectTest {

    @Test
    public void testSplitImage() {
        List<Roi> rois = LaneDetect.splitImage(100, 128);
        assertTrue(rois.stream().allMatch(roi -> roi.getBounds().width == 100));
        //assertEquals(128, rois.stream().mapToInt(roi -> roi.getBounds().height).sum());
    }

}