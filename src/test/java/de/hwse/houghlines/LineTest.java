package de.hwse.houghlines;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    public void positiveDistanceTest() {
        IntStream.range(-180, 180)
                .mapToObj(angle -> new Line(angle, 10))
                .map(line -> Pair.of(line, line.withPositiveDistance()))
                .forEach(pair -> assertEquals(pair.getFirst().pointAt(0), pair.getSecond().pointAt(0)));
    }

    @Test
    public void atXTest() {
        Line l = new Line(0, 100);
        assertEquals(100,l.yAt(100.0));

        Line l2 = new Line(90+45, 0);
        assertEquals(100, Math.round(l2.yAt(100)));
    }

    @Test
    public void easyTest() {
        Line l = new Line(45, 100);
        Position p0 = l.positionAtX(200).translate(0, -50);

        Line tranlated = l.translate(0, -50);
        Position p1 = tranlated.positionAtX(200);
        assertEquals(p0, p1);
    }

    Stream<Position> translationsStream() {
        return IntStream.range(-10, 10).boxed()
                .flatMap(deltaX -> IntStream.range(-10, 10).mapToObj(deltaY -> new Position(deltaX, deltaY)));
    }

    @Test
    public void testTranslate() {
        IntStream.range(-170, 170).boxed()
                .flatMap(angle -> IntStream.range(-10, 10).mapToObj(dist -> Pair.of(angle, dist)))
                .map(pair -> new Line(pair.getFirst(), pair.getSecond()))
                .flatMap(l -> translationsStream().map(trans -> Pair.of(l, trans)))
                .forEach(pair -> {
                    Line line = pair.getFirst();
                    Position translation = pair.getSecond();
                    double xDelta = translation.x();
                    double yDelta = translation.y();

                    // check 1: tranlate in y direction -> same x -> results in y + yDelta
                    Line yTranslated = line.translate(0, yDelta);
                    Position expected = line.positionAtX(0).translate(0, yDelta);
                    if (Double.isNaN(expected.x()) || line.angle == 0) return;
                    Position actual = yTranslated.positionAtX(0);
                    assertEquals(expected, actual, pair::toString);

                    // check 2: translate in x direction TODO
                });
    }

    @Test
    public void sanityTest() {
        IntStream.range(-359, 359)
                .mapToObj(angle -> new Line(angle, 100))
                .forEach(line -> {
                    Line pos = line.withPositiveAngle();
                    assertTrue(pos.angle >= 0);

                    Line sane = line.sanitizeAngle();
                    assertTrue(0.0 <= sane.angle && sane.angle < 180.0);
                });
    }

    @Test
    public void angleDiffTest() {
        IntStream.range(-359, 359).boxed()
                .flatMap(angle0 -> IntStream.range(-359, 359).mapToObj(angle1 ->Pair.of(angle0, angle1)))
                .forEach(pair -> {
                    Line l0 = new Line(pair.getFirst(), 100);
                    Line l1 = new Line(pair.getSecond(), 50);
                    double diff = l0.angleDiff(l1);
                    assertTrue(0.0 <= diff && diff < 180.0,
                            "diff == " + diff + " for " + l0 + " and " + l1);
                });
    }

}