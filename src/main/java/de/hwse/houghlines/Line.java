package de.hwse.houghlines;

import java.awt.*;
import java.nio.channels.Pipe;

public class Line {
    public static final Line ZERO_ANGLE = new Line(0.0, 1.0);

    public final double angle;
    public final double distance;

    public Line(double angle, double distance) {
        if (angle <= -360.0 || angle >= 360.0)
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

    double yAt(double x) {
        if (angle == 0.0 || angle == 180.0 || angle == -180.0) {
            return distance;
        } else if (angle == 90.0 || angle == 270.0 || angle == -90 || angle == -280.0) {
            return Double.NaN;
        }
        return -Math.cos(phi()) / Math.sin(phi()) * x + distance / Math.sin(phi());
    }

    Position positionAtX(double x) {
        return new Position(x, yAt(x));
    }

    double xAt(double y) {
        if (angle == 0.0 || angle == 180.0 || angle == -180) {
            return Double.NaN;
        } else if (angle == 90.0 || angle == 270 || angle == -90.0 || angle == -270.0) {
            return distance;
        }
        return -Math.sin(phi())/Math.cos(phi()) * y + distance / Math.cos(phi());
    }

    Position positionAtY(double y) {
        return new Position(xAt(y), y);
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

    /**
     * Equal representation with a minimal positive angle.
     */
    Line sanitizeAngle() {
        Line pos = this.withPositiveAngle();
        if (pos.angle >= 180.0) {
            return new Line(angle - 180, -distance);
        }
        return pos;
    }

    Line withPositiveAngle() {
        if (angle < 0) {
            Line inc = new Line(angle + 180.0, -distance);
            return inc.withPositiveAngle();
        } else {
            return this;
        }
    }

    double angleDiff(Line other) {
        Line l0 = this.sanitizeAngle();
        Line l1 = other.sanitizeAngle();
        double diff = Math.abs(l1.angle - l0.angle);
        return diff % 180.0;
    }

    Line translate(double x, double y) {
        return new Line(angle, distance +
                Math.cos(phi()) * x + Math.sin(phi()) * y);
    }

}
