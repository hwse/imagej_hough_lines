package de.hwse.houghlines;

import org.junit.jupiter.api.Test;

import java.awt.*;
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
        assertEquals(0,l.atX(100.0));

        Line l2 = new Line(90+45, 0);
        assertEquals(100, Math.round(l2.atX(100)));
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
        return IntStream.range(-100, 100).boxed()
                .flatMap(deltaX -> IntStream.range(-100, 100).mapToObj(deltaY -> new Position(deltaX, deltaY)));
    }

    @Test
    public void testTranslate() {
        /*IntStream.range(-180, 180).boxed()
                .flatMap(angle -> IntStream.range(-100, 100).mapToObj(dist -> Pair.of(angle, dist)))
                .map(pair -> new Line(pair.getFirst(), pair.getSecond()))
                .flatMap(l -> translationsStream().map(trans -> Pair.of(l, trans)))
                .forEach(pair -> {
                    Line line = pair.getFirst();
                    Position translation = pair.getSecond();

                    Position expected = line.positionAtX(100.0).translate(translation);

                    Line translated = line.translate(translation.x(), translation.y());
                    Position actual = translated.positionAtX(100.0+translation.x());

                    if (!expected.almostEquals(actual)) {
                        String.valueOf(1);
                    }
                    assertTrue(expected.almostEquals(actual), expected + " != " + actual + pair);
                });*/
    }

}