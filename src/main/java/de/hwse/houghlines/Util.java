package de.hwse.houghlines;

public class Util {
    protected static double angleToRad(double angle) {
        return angle * 2 * Math.PI / 360;
    }

    protected static double radToAngle(double rad) {
        return rad * 360 / (2*Math.PI);
    }
}
