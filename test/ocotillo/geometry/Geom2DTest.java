/**
 * Copyright © 2014-2015 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.geometry;

import ocotillo.geometry.Geom2D.CollinearLinesException;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Geom2DTest {

    @Test
    public void testModule() {
        assertThat(Geom2D.magnitude(new Coordinates(1, 1)), isAlmost(Math.sqrt(2)));
        assertThat(Geom2D.magnitude(new Coordinates(3, 4)), isAlmost(5));
        assertThat(Geom2D.magnitude(new Coordinates(Math.sqrt(2), Math.sqrt(2))), isAlmost(2));
        assertThat(Geom2D.magnitude(new Coordinates(7, 3)), isAlmost(7.61577310586));
    }

    @Test
    public void testDotProduct() {
        Coordinates a = new Coordinates(2, 1);
        Coordinates b = new Coordinates(3, 4);
        Coordinates c = new Coordinates(-3, 5);
        assertThat(Geom2D.dotProduct(a, b), isAlmost(10));
        assertThat(Geom2D.dotProduct(b, c), isAlmost(11));
        assertThat(Geom2D.dotProduct(a, c), isAlmost(-1));
        assertThat(Geom2D.dotProduct(a, c), isAlmost(Geom2D.dotProduct(c, a)));
    }

    @Test
    public void testAngle() {
        Coordinates a = new Coordinates(2, 2);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(1, Math.sqrt(3));
        Coordinates d = new Coordinates(-3, 5);
        Coordinates e = new Coordinates(0, 5);
        Coordinates f = new Coordinates(0, -5);
        Coordinates g = new Coordinates(0, 0);
        assertThat(Geom2D.angle(a), isAlmost(Math.PI / 4));
        assertThat(Geom2D.angle(b), isAlmost(0));
        assertThat(Geom2D.angle(c), isAlmost(Math.PI / 3));
        assertThat(Geom2D.angle(d), isAlmost(2.11121582707));
        assertThat(Geom2D.angle(e), isAlmost(Math.PI / 2));
        assertThat(Geom2D.angle(f), isAlmost(3 * Math.PI / 2));
        assertThat(Geom2D.angle(g), isAlmost(-1));
    }

    @Test
    public void testUnitVector() {
        assertThat(Geom2D.unitVector(0), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.unitVector(Math.PI / 2), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(Math.PI / 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 11)));
        assertThat(Geom2D.unitVector(new Coordinates(12, 0)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.unitVector(new Coordinates(0, -5)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.unitVector(new Coordinates(12, 0), new Coordinates(12, -5)), isAlmost(new Coordinates(12, -4)));
    }

    @Test
    public void testScaleVector() {
        assertThat(Geom2D.scaleVector(new Coordinates(1, 0), 2), isAlmost(new Coordinates(2, 0)));
        assertThat(Geom2D.scaleVector(new Coordinates(0, -1), 3), isAlmost(new Coordinates(0, -3)));
        assertThat(Geom2D.scaleVector(new Coordinates(10, 11), 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 12)));
    }

    @Test
    public void testRotateVector() {
        assertThat(Geom2D.rotateVector(new Coordinates(-1, 0), Math.PI), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.rotateVector(new Coordinates(1, 0), Math.PI / 2), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.rotateVector(new Coordinates(11, 10), Math.PI / 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 11)));
    }

    @Test
    public void testBetweenAngle() {
        assertThat(Geom2D.betweenAngle(new Coordinates(-1, 0), new Coordinates(1, 0)), isAlmost(Math.PI));
        assertThat(Geom2D.betweenAngle(new Coordinates(-1, 0), new Coordinates(0, 1)), isAlmost(Math.PI / 2));
        assertThat(Geom2D.betweenAngle(new Coordinates(-11, 10), new Coordinates(11, 10), new Coordinates(10, 10)), isAlmost(Math.PI));
    }

    @Test
    public void testBisector() {
        assertThat(Geom2D.bisector(new Coordinates(1, 0), new Coordinates(0, 1)), isAlmost(new Coordinates(Math.sqrt(2) / 2, Math.sqrt(2) / 2)));
        assertThat(Geom2D.bisector(Geom2D.unitVector(0.3), Geom2D.unitVector(1.1)), isAlmost(Geom2D.unitVector(0.7)));
        assertThat(Geom2D.bisector(new Coordinates(11, 10), new Coordinates(10, 11), new Coordinates(10, 10)), isAlmost(new Coordinates(10 + Math.sqrt(2) / 2, 10 + Math.sqrt(2) / 2)));
    }

    @Test
    public void testPointOnLineProjection() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        Coordinates y = new Coordinates(0, 1);
        Coordinates a = new Coordinates(5, 5);
        Coordinates b = new Coordinates(-2.5, -2.5);
        assertThat(Geom2D.pointOnLineProjection(x, o, x), isAlmost(x));
        assertThat(Geom2D.pointOnLineProjection(n, o, x), isAlmost(new Coordinates(3, 0)));
        assertThat(Geom2D.pointOnLineProjection(n, o, y), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.pointOnLineProjection(n, a, b), isAlmost(new Coordinates(2, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPointOnLineProjectionIAE() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Geom2D.pointOnLineProjection(n, o, o);
    }

    @Test
    public void testPointToLineDistance() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        Coordinates y = new Coordinates(0, 1);
        Coordinates a = new Coordinates(5, 5);
        Coordinates b = new Coordinates(-2.5, -2.5);
        assertThat(Geom2D.pointToLineDistance(x, o, x), isAlmost(0));
        assertThat(Geom2D.pointToLineDistance(n, o, x), isAlmost(1));
        assertThat(Geom2D.pointToLineDistance(n, o, y), isAlmost(3));
        assertThat(Geom2D.pointToLineDistance(n, a, b), isAlmost(Math.sqrt(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPointToLineDistanceIAE() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Geom2D.pointToLineDistance(n, o, o);
    }

    @Test
    public void testIsPointInLine() {
        Coordinates n = new Coordinates(2, 2);
        Coordinates a = new Coordinates(2, -2);
        Coordinates b = new Coordinates(2, -6);
        Coordinates c = new Coordinates(0, 1);
        Coordinates d = new Coordinates(4, 3);
        Coordinates e = new Coordinates(3, 0);
        Coordinates f = new Coordinates(4, -2);
        assertThat(Geom2D.isPointInLine(n, a, b), is(true));
        assertThat(Geom2D.isPointInLine(n, c, d), is(true));
        assertThat(Geom2D.isPointInLine(n, e, f), is(true));
        assertThat(Geom2D.isPointInLine(n, a, f), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPointInLineIAE() {
        Coordinates n = new Coordinates(2, 2);
        Coordinates a = new Coordinates(2, -2);
        Geom2D.isPointInLine(n, a, a);
    }

    @Test
    public void testPointOnSegmentProjection() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        Coordinates y = new Coordinates(0, 1);
        Coordinates a = new Coordinates(5, 5);
        Coordinates b = new Coordinates(7, 7);
        assertThat(Geom2D.pointOnSegmentProjection(x, o, x), isAlmost(x));
        assertThat(Geom2D.pointOnSegmentProjection(n, o, x), is(nullValue()));
        assertThat(Geom2D.pointOnSegmentProjection(n, o, y), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.pointOnSegmentProjection(n, a, b), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPointOnSegmentProjectionIAE() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Geom2D.pointOnSegmentProjection(n, o, o);
    }

    @Test
    public void testPointToSegmentDistance() {
        Coordinates n = new Coordinates(3, 1);
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        Coordinates y = new Coordinates(0, 1);
        Coordinates a = new Coordinates(5, 5);
        Coordinates b = new Coordinates(-2.5, -2.5);
        Coordinates c = new Coordinates(7, 7);
        assertThat(Geom2D.pointToSegmentDistance(n, o, o), isAlmost(Geom2D.magnitude(n)));
        assertThat(Geom2D.pointToSegmentDistance(x, o, x), isAlmost(0));
        assertThat(Geom2D.pointToSegmentDistance(n, o, x), isAlmost(Geom2D.magnitude(n.minus(x))));
        assertThat(Geom2D.pointToSegmentDistance(n, o, y), isAlmost(3));
        assertThat(Geom2D.pointToSegmentDistance(n, a, b), isAlmost(Math.sqrt(2)));
        assertThat(Geom2D.pointToSegmentDistance(n, a, c), isAlmost(Geom2D.magnitude(n.minus(a))));
    }

    @Test
    public void testclosestSegmentPoint() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(-5, 0);
        Coordinates d = new Coordinates(0.5, 10);
        Coordinates e = new Coordinates(3, 2);
        Coordinates f = new Coordinates(0.7, 0);
        assertThat(Geom2D.closestSegmentPoint(c, a, b), isAlmost(a));
        assertThat(Geom2D.closestSegmentPoint(d, a, b), isAlmost(new Coordinates(0.5, 0)));
        assertThat(Geom2D.closestSegmentPoint(e, a, b), isAlmost(b));
        assertThat(Geom2D.closestSegmentPoint(f, a, b), isAlmost(f));
    }

    @Test
    public void testIsPointInSegment() {
        Coordinates n = new Coordinates(2, 2);
        Coordinates a = new Coordinates(2, -2);
        Coordinates b = new Coordinates(2, -6);
        Coordinates c = new Coordinates(0, 1);
        Coordinates d = new Coordinates(4, 3);
        Coordinates e = new Coordinates(3, 0);
        Coordinates f = new Coordinates(4, -2);
        Coordinates g = new Coordinates(8, 8);
        assertThat(Geom2D.isPointInSegment(n, n, n), is(true));
        assertThat(Geom2D.isPointInSegment(n, a, n), is(true));
        assertThat(Geom2D.isPointInSegment(n, n, a), is(true));
        assertThat(Geom2D.isPointInSegment(n, a, a), is(false));
        assertThat(Geom2D.isPointInSegment(n, a, b), is(false));
        assertThat(Geom2D.isPointInSegment(n, c, d), is(true));
        assertThat(Geom2D.isPointInSegment(n, e, f), is(false));
        assertThat(Geom2D.isPointInSegment(n, a, f), is(false));
        assertThat(Geom2D.isPointInSegment(n, g, g.minus()), is(true));
    }

    @Test
    public void testLineLineIntersection() {
        Coordinates a = new Coordinates(0, -2);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(4, 0);
        Coordinates d = new Coordinates(0, 4);
        Coordinates e = new Coordinates(3, 7);
        Coordinates f = new Coordinates(5, 7);
        assertThat(Geom2D.lineLineIntersection(b, c, e, f), is(nullValue()));
        assertThat(Geom2D.lineLineIntersection(a, b, c, d), isAlmost(new Coordinates(2, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLineLineIntersectionIAE() {
        Coordinates a = new Coordinates(0, -2);
        Coordinates c = new Coordinates(4, 0);
        Coordinates d = new Coordinates(0, 4);
        Geom2D.lineLineIntersection(a, a, c, d);
    }

    @Test(expected = CollinearLinesException.class)
    public void testLineLineIntersectionCLE() {
        Coordinates e = new Coordinates(3, 7);
        Coordinates f = new Coordinates(5, 7);
        Coordinates g = new Coordinates(9, 7);
        Coordinates h = new Coordinates(4, 7);
        Geom2D.lineLineIntersection(e, f, g, h);
    }

    @Test
    public void testLineSegmIntersection() {
        Coordinates a = new Coordinates(0, -2);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(4, 0);
        Coordinates d = new Coordinates(0, 4);
        Coordinates e = new Coordinates(3, 7);
        Coordinates f = new Coordinates(5, 7);
        assertThat(Geom2D.lineSegmIntersection(a, d, a, a), isAlmost(a));
        assertThat(Geom2D.lineSegmIntersection(b, c, e, f), is(nullValue()));
        assertThat(Geom2D.lineSegmIntersection(a, b, c, d), isAlmost(new Coordinates(2, 2)));
        assertThat(Geom2D.lineSegmIntersection(c, d, a, b), is(nullValue()));
        assertThat(Geom2D.lineSegmIntersection(c, d, a, a), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLineSegmIntersectionIAE() {
        Coordinates a = new Coordinates(0, -2);
        Coordinates c = new Coordinates(4, 0);
        Coordinates d = new Coordinates(0, 4);
        Geom2D.lineSegmIntersection(a, a, c, d);
    }

    @Test(expected = CollinearLinesException.class)
    public void testLineSegmIntersectionCLE() {
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(4, 0);
        Geom2D.lineSegmIntersection(b, c, b, c);
    }

    @Test
    public void testSegmSegmIntersection() {
        Coordinates a = new Coordinates(0, -2);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(4, 0);
        Coordinates d = new Coordinates(0, 4);
        Coordinates e = new Coordinates(3, 7);
        Coordinates f = new Coordinates(5, 7);
        Coordinates g = new Coordinates(0, 0);
        Coordinates h = new Coordinates(4, 4);
        assertThat(Geom2D.segmSegmIntersection(a, a, c, d), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(c, d, a, a), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(b, b.times(5), b.times(7), b.times(9)), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(a, d, a, a), isAlmost(a));
        assertThat(Geom2D.segmSegmIntersection(a, a, d, a), isAlmost(a));
        assertThat(Geom2D.segmSegmIntersection(b, c, e, f), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(a, b, c, d), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(c, d, a, b), is(nullValue()));
        assertThat(Geom2D.segmSegmIntersection(g, h, c, d), isAlmost(new Coordinates(2, 2)));
        assertThat(Geom2D.segmSegmIntersection(d, c, h, g), isAlmost(new Coordinates(2, 2)));
    }

    @Test(expected = CollinearLinesException.class)
    public void testSegmSegmIntersectionCLE() {
        Coordinates b = new Coordinates(1, 0);
        Geom2D.lineSegmIntersection(b, b.times(7), b.times(5), b.times(9));
    }

    @Test
    public void testIsPolygonSimple() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(0, 0));
        polygonD.add(new Coordinates(0, 1));

        Polygon polygonE = new Polygon();
        polygonE.add(new Coordinates(0, 0));
        polygonE.add(new Coordinates(0, 1));
        polygonE.add(new Coordinates(1, 1));

        Polygon polygonF = new Polygon();
        polygonF.add(new Coordinates(0, 0));
        polygonF.add(new Coordinates(0, 1));
        polygonF.add(new Coordinates(1, 0));
        polygonF.add(new Coordinates(1, 1));

        assertThat(Geom2D.isPolygonSimple(polygonA), is(true));
        assertThat(Geom2D.isPolygonSimple(polygonB), is(true));
        assertThat(Geom2D.isPolygonSimple(polygonC), is(false));
        assertThat(Geom2D.isPolygonSimple(polygonD), is(false));
        assertThat(Geom2D.isPolygonSimple(polygonE), is(true));
        assertThat(Geom2D.isPolygonSimple(polygonF), is(false));
    }

    @Test
    public void testPolygonArea() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 1));

        assertThat(Geom2D.polygonArea(polygonA), isAlmost(54.5));
        assertThat(Geom2D.polygonArea(polygonB), isAlmost(1));
        assertThat(Geom2D.polygonArea(polygonC), isAlmost(0.5));
    }

    @Test
    public void testPolygonSignedArea() {
        Polygon anticlockwise = new Polygon();
        anticlockwise.add(new Coordinates(0, 0));
        anticlockwise.add(new Coordinates(0, 1));
        anticlockwise.add(new Coordinates(1, 1));
        anticlockwise.add(new Coordinates(1, 0));

        Polygon clockwise = new Polygon();
        clockwise.add(new Coordinates(0, 0));
        clockwise.add(new Coordinates(1, 0));
        clockwise.add(new Coordinates(1, 1));
        clockwise.add(new Coordinates(0, 1));

        assertThat(Geom2D.polygonSignedArea(anticlockwise), isAlmost(1));
        assertThat(Geom2D.polygonSignedArea(clockwise), isAlmost(-1));
    }

    @Test
    public void testPolygonPerimeter() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(0, 0));
        polygonA.add(new Coordinates(0, 1));
        polygonA.add(new Coordinates(1, 1));
        polygonA.add(new Coordinates(1, 0));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(1, 0));
        polygonB.add(new Coordinates(2, 1));
        polygonB.add(new Coordinates(2, -1));
        polygonB.add(new Coordinates(1, -1));

        assertThat(Geom2D.polygonPerimeter(polygonA), isAlmost(4));
        assertThat(Geom2D.polygonPerimeter(polygonB), isAlmost(4 + 2 * Math.sqrt(2)));
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE1() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        Geom2D.polygonArea(polygon);
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE2() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        Geom2D.polygonArea(polygon);
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE3() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        polygon.add(new Coordinates(1, 0));
        polygon.add(new Coordinates(1, 1));
        Geom2D.polygonArea(polygon);
    }

    @Test
    public void testPolygonCentroid() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(5, 1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 0));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(0, 0));
        polygonD.add(new Coordinates(1, 0));
        polygonD.add(new Coordinates(0, 1));

        assertThat(Geom2D.polygonCentroid(polygonA), isAlmost(new Coordinates(13.0 / 33.0, 2)));
        assertThat(Geom2D.polygonCentroid(polygonB), isAlmost(new Coordinates(0.5, 0.5)));
        assertThat(Geom2D.polygonCentroid(polygonC), isAlmost(new Coordinates(1.0 / 3.0, 1.0 / 3)));
        assertThat(Geom2D.polygonCentroid(polygonD), isAlmost(Geom2D.polygonCentroid(polygonC)));
    }

    @Test
    public void testIsPointInPolygonBoundary() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        polygon.add(new Coordinates(1, 0));
        polygon.add(new Coordinates(1, 1));
        assertThat(Geom2D.isPointInPolygonBoundary(new Coordinates(0.7, 0.5), polygon), is(false));
        assertThat(Geom2D.isPointInPolygonBoundary(new Coordinates(-1, 1), polygon), is(false));
        assertThat(Geom2D.isPointInPolygonBoundary(new Coordinates(0, 0.5), polygon), is(true));
        assertThat(Geom2D.isPointInPolygonBoundary(new Coordinates(0, 0), polygon), is(true));
    }

    @Test
    public void testIsPointInPolygon() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, 3));
        polygonA.add(new Coordinates(5, 4));
        polygonA.add(new Coordinates(6, 2));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 1));
        polygonC.add(new Coordinates(1, 0));

        assertThat(Geom2D.isPointInPolygon(new Coordinates(1, 1), polygonA), is(true));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(10, 10), polygonA), is(false));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(-10, -10), polygonA), is(false));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(0.7, 0.5), polygonB), is(true));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(-1, 1), polygonB), is(false));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(3, 3), polygonC), is(false));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(-3, -3), polygonC), is(false));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(0, 0.5), polygonB), is(true));
        assertThat(Geom2D.isPointInPolygon(new Coordinates(0, 0), polygonB), is(true));
    }

    @Test
    public void testConvexHull() {
        List<Coordinates> pointsA = new ArrayList<>();
        pointsA.add(new Coordinates(-2, -3));
        pointsA.add(new Coordinates(-5, 1));
        pointsA.add(new Coordinates(3, 8));
        pointsA.add(new Coordinates(4, 3));
        pointsA.add(new Coordinates(5, 4));
        pointsA.add(new Coordinates(6, 2));

        List<Coordinates> pointsB = new ArrayList<>();
        pointsB.add(new Coordinates(0, 0));
        pointsB.add(new Coordinates(0, 1));
        pointsB.add(new Coordinates(1, 1));
        pointsB.add(new Coordinates(1, 0));

        List<Coordinates> pointsC = new ArrayList<>();
        pointsC.add(new Coordinates(0, 0));
        pointsC.add(new Coordinates(0, 0));
        pointsC.add(new Coordinates(0, 1));
        pointsC.add(new Coordinates(1, 1));
        pointsC.add(new Coordinates(1, 0));

        List<Coordinates> pointsD = new ArrayList<>();

        List<Coordinates> pointsE = new ArrayList<>();
        pointsE.add(new Coordinates(0, 0));

        List<Coordinates> pointsF = new ArrayList<>();
        pointsF.add(new Coordinates(0, 0));
        pointsF.add(new Coordinates(0, 0));

        List<Coordinates> convexHullA = Geom2D.convexHull(pointsA);
        assertThat(convexHullA.size(), is(4));
        assertThat(convexHullA.get(0), is(new Coordinates(-2, -3)));
        assertThat(convexHullA.get(1), is(new Coordinates(6, 2)));
        assertThat(convexHullA.get(2), is(new Coordinates(3, 8)));
        assertThat(convexHullA.get(3), is(new Coordinates(-5, 1)));

        List<Coordinates> convexHullB = Geom2D.convexHull(pointsB);
        assertThat(convexHullB.size(), is(4));
        assertThat(convexHullB.get(0), is(new Coordinates(0, 0)));
        assertThat(convexHullB.get(1), is(new Coordinates(1, 0)));
        assertThat(convexHullB.get(2), is(new Coordinates(1, 1)));
        assertThat(convexHullB.get(3), is(new Coordinates(0, 1)));

        List<Coordinates> convexHullC = Geom2D.convexHull(pointsC);
        assertThat(convexHullC.size(), is(4));
        assertThat(convexHullC.get(0), is(new Coordinates(0, 0)));
        assertThat(convexHullC.get(1), is(new Coordinates(1, 0)));
        assertThat(convexHullC.get(2), is(new Coordinates(1, 1)));
        assertThat(convexHullC.get(3), is(new Coordinates(0, 1)));

        List<Coordinates> convexHullD = Geom2D.convexHull(pointsD);
        assertThat(convexHullD, empty());

        List<Coordinates> convexHullE = Geom2D.convexHull(pointsE);
        assertThat(convexHullE.size(), is(1));
        assertThat(convexHullE.get(0), is(new Coordinates(0, 0)));

        List<Coordinates> convexHullF = Geom2D.convexHull(pointsF);
        assertThat(convexHullF.size(), is(1));
        assertThat(convexHullF.get(0), is(new Coordinates(0, 0)));
    }
}
