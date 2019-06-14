package de.hwse.houghlines;

import java.util.ArrayList;
import java.util.List;

class Accumulator {
    private static final int MAX_ANGLE = 180;

    private final int maxDistance;
    private final int[][] accArray;

    public Accumulator(int width, int height) {
        maxDistance = (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
        accArray = new int[MAX_ANGLE][2*maxDistance+1];
    }

    private void incCount(int angle, int distance) {
        accArray[angle][distance+maxDistance] += 1;
    }

    private int getCount(int angle, int distance) {
        return accArray[angle][distance+maxDistance];
    }

    public void countPoint(int x , int y) {
        for(int angle = 0; angle < MAX_ANGLE; angle++) {
            double rad = Util.angleToRad(angle);
            double distance = x * Math.cos(rad) + y * Math.sin(rad);

            incCount(angle, Math.toIntExact(Math.round(distance)));
        }
    }

    public List<Line> getAboveThreshold(int threshold) {
        List<Line> result = new ArrayList<>();
        for (int angle = 0; angle < MAX_ANGLE; angle++) {
            for (int distance = -maxDistance; distance <= maxDistance; distance++) {
                int count = getCount(angle, distance);
                if (count > threshold) {
                    result.add(new Line(angle, distance));
                }
            }
        }
        return result;
    }
}
