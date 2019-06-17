package de.hwse.houghlines;

import java.awt.*;

public class Line {
    public final double angle;
    public final double distance;

    public Line(double angle, double distance) {
        this.angle = angle;
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        if (Double.compare(line.angle, angle) != 0) return false;
        return Double.compare(line.distance, distance) == 0;
    }

    @Override
    public String toString() {
        return "Line{" +
                "angle=" + angle +
                ", distance=" + distance +
                '}';
    }

    Point pointAt(double factor) {
        double a = Math.cos(Util.angleToRad(angle));
        double b = Math.sin(Util.angleToRad(angle));
        double x0 = a * distance;
        double y0 = b * distance;
        return new Point(
                Math.toIntExact(Math.round(x0 + factor * (-b))),
                Math.toIntExact(Math.round(y0 + factor * a))
        );
    }

}
