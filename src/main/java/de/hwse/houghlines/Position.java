package de.hwse.houghlines;

import java.awt.*;

public class Position {
    private static final double MAX_EQUAL_DIFF = 0.00001;
    private final double x;
    private final double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double y() {
        return x;
    }

    public double x() {
        return y;
    }

    public Position translate(double x, double y) {
        return new Position(this.x + x, this.y + y);
    }

    public Position translate(Position delta) {
        return translate(delta.x, delta.y);
    }

    Point roundToPoint() {
        return new Point(Math.toIntExact(Math.round(x)), Math.toIntExact(Math.round(y)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (Double.compare(position.x, x) != 0) return false;
        return Double.compare(position.y, y) == 0;
    }

    public boolean almostEquals(Position other) {
        double xDiff = Math.abs(x - other.x);
        double yDiff = Math.abs(y - other.y);
        return xDiff < MAX_EQUAL_DIFF && yDiff < MAX_EQUAL_DIFF;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
