package de.hwse.houghlines;

import java.awt.*;

public class Line {
    public final double angle;
    public final double distance;

    public Line(double angle, double distance) {
        if (angle <= -360 || angle >= 360)
            throw new IllegalArgumentException("pass some good angle bitch");
        this.angle = angle;
        this.distance = distance;
    }

    double phi() {
        return Util.angleToRad(angle);
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

    double atX(double x) {
        if (angle == 0.0) {
            return 0.0;
        }
        return -Math.cos(phi())/Math.sin(phi()) * x + distance / Math.sin(phi());
    }

    Position positionAtX(double x) {
        return new Position(x, atX(x));
    }

    /**
     * Equal representation of this line with a positive distance.
     */
    Line withPositiveDistance() {
        if (distance < 0) {
            return new Line(angle-180.0, -distance);
        } else {
            return this;
        }
    }

    Line translate(double x, double y) {
        return new Line(angle, distance +
                Math.cos(phi()) * x + Math.sin(phi()) * y);
    }

}
