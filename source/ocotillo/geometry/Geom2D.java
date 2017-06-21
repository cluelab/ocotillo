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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects the geometry methods that operates on 2D vectorial spaces.
 */
public class Geom2D extends GeomXD {

    /**
     * Exception indicating that an intersection cannot be computed as two lines
     * or segments overlap.
     */
    public static class CollinearLinesException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        public CollinearLinesException(String message) {
            super(message);
        }
    }

    /**
     * Checks if two coordinates are almost equal.
     *
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @return true if almost equal, false if not.
     */
    public static boolean almostEqual(Coordinates a, Coordinates b) {
        return almostEqual(a, b, epsilon);
    }

    /**
     * Checks if two coordinates are almost equal.
     *
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @param epsilon the tolerance.
     * @return true if almost equal, false if not.
     */
    public static boolean almostEqual(Coordinates a, Coordinates b, double epsilon) {
        return almostEqual(a.x(), b.x(), epsilon) && almostEqual(a.y(), b.y(), epsilon);
    }

    /**
     * Returns the magnitude of a vector.
     *
     * @param vector the coordinates of a vector.
     * @return the vector's magnitude.
     */
    public static double magnitude(Coordinates vector) {
        return Math.sqrt(vector.x() * vector.x() + vector.y() * vector.y());
    }

    /**
     * Returns the dot product of two vectors.
     *
     * @param vectorA the coordinates of the first vector.
     * @param vectorB the coordinates of the second vector.
     * @return the dot product.
     */
    public static double dotProduct(Coordinates vectorA, Coordinates vectorB) {
        return vectorA.x() * vectorB.x() + vectorA.y() * vectorB.y();
    }

    /**
     * Returns the angle (in radians) of the vector with given coordinates.
     *
     * @param vector the coordinates of the vector.
     * @return the calculated angle.
     */
    public static double angle(Coordinates vector) {
        double angle = -1.0;
        double x = vector.x();
        double y = vector.y();
        if (x > 0 && y >= 0) {
            angle = Math.atan(y / x);
        } else if (x > 0 && y < 0) {
            angle = Math.atan(y / x) + 2.0 * Math.PI;
        } else if (x < 0) {
            angle = Math.atan(y / x) + Math.PI;
        } else if (GeomXD.almostEqual(x, 0) && y > 0) {
            angle = Math.PI / 2.0;
        } else if (GeomXD.almostEqual(x, 0) && y < 0) {
            angle = Math.PI * 3.0 / 2.0;
        }
        return angle;
    }

    /**
     * Creates the unit vector with given angle (in radians).
     *
     * @param angle the angle.
     * @return the unit vector.
     */
    public static Coordinates unitVector(double angle) {
        return new Coordinates(Math.cos(angle), Math.sin(angle));
    }

    /**
     * Creates the unit vector with given angle (in radians).
     *
     * @param angle the angle.
     * @param origin the origin of the vector
     * @return the unit vector.
     */
    public static Coordinates unitVector(double angle, Coordinates origin) {
        return unitVector(angle).plusIP(origin);
    }

    /**
     * Creates the unit vector obtained by rescaling a given one.
     *
     * @param vector the vector.
     * @return its unit vector.
     */
    public static Coordinates unitVector(Coordinates vector) {
        return scaleVector(vector, 1 / magnitude(vector));
    }

    /**
     * Creates the unit vector obtained by rescaling a given one.
     *
     * @param vector the vector.
     * @param origin the origin of the vector
     * @return its unit vector.
     */
    public static Coordinates unitVector(Coordinates vector, Coordinates origin) {
        return unitVector(vector.minus(origin)).plusIP(origin);
    }

    /**
     * Scales a vector by a given factor.
     *
     * @param vector the vector.
     * @param factor the factor.
     * @return the scaled vector.
     */
    public static Coordinates scaleVector(Coordinates vector, double factor) {
        return vector.times(factor);
    }

    /**
     * Scales a vector with given origin by a given factor.
     *
     * @param vector the vector.
     * @param factor the factor.
     * @param origin the origin of the vector.
     * @return the scaled vector.
     */
    public static Coordinates scaleVector(Coordinates vector, double factor, Coordinates origin) {
        return scaleVector(vector.minus(origin), factor).plusIP(origin);
    }

    /**
     * Rotates a vector by the given angle (in radians).
     *
     * @param vector the vector.
     * @param angle the angle.
     * @return the rotated vector.
     */
    public static Coordinates rotateVector(Coordinates vector, double angle) {
        double originalAngle = angle(vector);
        double magnitude = magnitude(vector);
        return unitVector(originalAngle + angle).timesIP(magnitude);
    }

    /**
     * Rotates a vector by the given angle (in radians).
     *
     * @param vector the vector.
     * @param angle the angle.
     * @param origin the origin of the vector.
     * @return the rotated vector.
     */
    public static Coordinates rotateVector(Coordinates vector, double angle, Coordinates origin) {
        return rotateVector(vector.minus(origin), angle).plusIP(origin);
    }

    /**
     * Returns the amplitude of the angle between two vectors.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @return the angle between them.
     */
    public static double betweenAngle(Coordinates a, Coordinates b) {
        double angleA = angle(a);
        double angleB = angle(b);
        return Math.abs(normalizeRadiansAngle(angleA - angleB));
    }

    /**
     * Returns the amplitude of the angle between two vectors with given origin.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @param origin the origin of the vectors.
     * @return the angle between them.
     */
    public static double betweenAngle(Coordinates a, Coordinates b, Coordinates origin) {
        return betweenAngle(a.minus(origin), b.minus(origin));
    }

    /**
     * Returns the unit vector that lays in the bisector of two vectors. The
     * resulting vector always lays on the smallest angle.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @return the unit vector on the bisector.
     */
    public static Coordinates bisector(Coordinates a, Coordinates b) {
        double angleA = angle(a);
        double angleB = angle(b);
        double bisecAngle = (angleA + angleB) / 2;
        if (Math.abs(normalizeRadiansAngle(bisecAngle - angleA)) > Math.PI / 2) {
            bisecAngle = bisecAngle + Math.PI;
        }
        return unitVector(bisecAngle);
    }

    /**
     * Returns the unit vector that lays in the bisector of two vectors. The
     * resulting vector always lays on the smallest angle.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @param origin the origin of the vectors.
     * @return the unit vector with given origin on the bisector.
     */
    public static Coordinates bisector(Coordinates a, Coordinates b, Coordinates origin) {
        return bisector(a.minus(origin), b.minus(origin)).plusIP(origin);
    }

    /**
     * Returns the orthogonal projection of a point in the line defined by two
     * other points.
     *
     * @param pointToProject the point to project.
     * @param linePointA the first point of the line.
     * @param linePointB the second point of the line.
     * @return the projection of the point in the line.
     */
    public static Coordinates pointOnLineProjection(Coordinates pointToProject, Coordinates linePointA, Coordinates linePointB) {
        Coordinates vectorAb = linePointB.minus(linePointA);
        double lengthAb = magnitude(vectorAb);
        if (GeomXD.almostEqual(0, lengthAb)) {
            throw new IllegalArgumentException("The line is not defined as its points coincide.");
        }

        Coordinates unitVectorAb = vectorAb.divideIP(lengthAb);
        return unitVectorAb.timesIP(dotProduct(pointToProject.minus(linePointA), unitVectorAb)).plusIP(linePointA);
    }

    /**
     * Returns the distance between a point and a line defined by two other
     * points.
     *
     * @param point the point.
     * @param linePointA the first point of the line.
     * @param linePointB the second point of the line.
     * @return the distance between the point and the line.
     */
    public static double pointToLineDistance(Coordinates point, Coordinates linePointA, Coordinates linePointB) {
        Coordinates projection = pointOnLineProjection(point, linePointA, linePointB);
        return magnitude(projection.minusIP(point));
    }

    /**
     * Verifies if a point belongs to a line.
     *
     * @param point the point.
     * @param linePointA the first point of the line.
     * @param linePointB the second point of the line.
     * @return true if the point belongs to the line, false if not.
     */
    public static boolean isPointInLine(Coordinates point, Coordinates linePointA, Coordinates linePointB) {
        return GeomXD.almostEqual(0, pointToLineDistance(point, linePointA, linePointB));
    }

    /**
     * Returns the orthogonal projection of a point in the segment defined by
     * two other points.
     *
     * @param pointToProject the point to project.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return null if the projection does not belong to the segment, the
     * projection otherwise.
     */
    public static Coordinates pointOnSegmentProjection(Coordinates pointToProject, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        Coordinates projection = pointOnLineProjection(pointToProject, segmExtremityA, segmExtremityB);
        return isPointInSegment(projection, segmExtremityA, segmExtremityB) ? projection : null;
    }

    /**
     * Returns the distance between a point and a segment defined by two other
     * points.
     *
     * @param point the point.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return the distance between the point and the segment.
     */
    public static double pointToSegmentDistance(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        if (almostEqual(segmExtremityA, segmExtremityB)) {
            return magnitude(point.minus(segmExtremityA));
        }

        Coordinates projection = pointOnSegmentProjection(point, segmExtremityA, segmExtremityB);
        return projection != null ? magnitude(point.minus(projection)) : Math.min(magnitude(point.minus(segmExtremityA)), magnitude(point.minus(segmExtremityB)));
    }

    /**
     * Returns the closest segment point to a given point.
     *
     * @param point the point.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return the closest segment point to the point passed as parameter.
     */
    public static Coordinates closestSegmentPoint(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        Coordinates projection = pointOnSegmentProjection(point, segmExtremityA, segmExtremityB);
        if (projection != null) {
            return projection;
        }
        if (magnitude(segmExtremityA.minus(point)) < magnitude(segmExtremityB.minus(point))) {
            return segmExtremityA;
        } else {
            return segmExtremityB;
        }
    }

    /**
     * Verifies if a point belongs to a segment defined by two other points.
     *
     * @param point the point.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return true if the point belongs to the segment, false if not.
     */
    public static boolean isPointInSegment(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        if (almostEqual(segmExtremityA, segmExtremityB)) {
            return almostEqual(point, segmExtremityA);
        }

        Coordinates pa = point.minus(segmExtremityA);
        Coordinates pb = point.minus(segmExtremityB);
        boolean isInBetween = magnitude(pa.plus(pb)) <= Math.max(magnitude(pa), magnitude(pb));
        return isInBetween && isPointInLine(point, segmExtremityA, segmExtremityB);
    }

    /**
     * Gets the intersection between two lines, whenever it exists. Throws
     * IllegalArgumentException when a line is undefined as its points are
     * coincident. Throws CollinearLinesException when the lines are collinear.
     *
     * @param lineAPoint1 The first point of line A.
     * @param lineAPoint2 The second point of line A.
     * @param lineBPoint1 The first point of line B.
     * @param lineBPoint2 The second point of line B.
     * @return Null if the lines are parallel, the intersection point otherwise.
     */
    public static Coordinates lineLineIntersection(Coordinates lineAPoint1, Coordinates lineAPoint2,
            Coordinates lineBPoint1, Coordinates lineBPoint2) {
        if (GeomXD.almostEqual(0, magnitude(lineAPoint1.minus(lineAPoint2)))
                || GeomXD.almostEqual(0, magnitude(lineBPoint1.minus(lineBPoint2)))) {
            throw new IllegalArgumentException("One of the lines is not defined as its points coincide.");
        }

        double[][] matrix = new double[2][2];
        matrix[0][0] = lineAPoint2.y() - lineAPoint1.y();
        matrix[0][1] = lineBPoint2.y() - lineBPoint1.y();
        matrix[1][0] = lineAPoint1.x() - lineAPoint2.x();
        matrix[1][1] = lineBPoint1.x() - lineBPoint2.x();
        double determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        if (GeomXD.almostEqual(determinant, 0)) {
            if (isPointInLine(lineAPoint1, lineBPoint1, lineBPoint2)) {
                throw new CollinearLinesException("The lines provided are collinear.");
            } else {
                return null;
            }
        }

        double coeff1 = matrix[0][0] * lineAPoint1.x() + matrix[1][0] * lineAPoint1.y();
        double coeff2 = matrix[0][1] * lineBPoint1.x() + matrix[1][1] * lineBPoint1.y();
        return new Coordinates(
                (matrix[1][1] * coeff1 - matrix[1][0] * coeff2) / determinant,
                (matrix[0][0] * coeff2 - matrix[0][1] * coeff1) / determinant);
    }

    /**
     * Gets the intersection between a line and a segment, whenever it exists.
     * Throws IllegalArgumentException when the line or segment are undefined as
     * their points are coincident. Throws CollinearLinesException when line and
     * segments are collinear.
     *
     * @param linePoint1 the first point of the line.
     * @param linePoint2 the second point of the line.
     * @param segmPoint1 the first point of the segment.
     * @param segmPoint2 the second point of the segment.
     * @return null if the line and the segment do not intersect, the
     * intersection otherwise.
     */
    public static Coordinates lineSegmIntersection(Coordinates linePoint1, Coordinates linePoint2,
            Coordinates segmPoint1, Coordinates segmPoint2) {
        if (almostEqual(segmPoint1, segmPoint2)) {
            return isPointInLine(segmPoint1, linePoint1, linePoint2) ? segmPoint1 : null;
        }

        Coordinates lineLineIntersection = lineLineIntersection(linePoint1, linePoint2, segmPoint1, segmPoint2);
        if (lineLineIntersection == null || !isPointInSegment(lineLineIntersection, segmPoint1, segmPoint2)) {
            return null;
        }
        return lineLineIntersection;
    }

    /**
     * Gets the intersection between two segments, whenever it exists. Throws
     * CollinearLinesException when the segments overlap.
     *
     * @param segmAPoint1 the first point of segment A.
     * @param segmAPoint2 the second point of segment A.
     * @param segmBPoint1 the first point of segment B.
     * @param segmBPoint2 the second point of segment B.
     * @return null if the segments do not intersect, the intersection
     * otherwise.
     */
    public static Coordinates segmSegmIntersection(Coordinates segmAPoint1, Coordinates segmAPoint2,
            Coordinates segmBPoint1, Coordinates segmBPoint2) {
        // Complitely separated, possibly collinear
        if (Math.max(segmAPoint1.x(), segmAPoint2.x()) < Math.min(segmBPoint1.x(), segmBPoint2.x())
                || Math.min(segmAPoint1.x(), segmAPoint2.x()) > Math.max(segmBPoint1.x(), segmBPoint2.x())
                || Math.max(segmAPoint1.y(), segmAPoint2.y()) < Math.min(segmBPoint1.y(), segmBPoint2.y())
                || Math.min(segmAPoint1.y(), segmAPoint2.y()) > Math.max(segmBPoint1.y(), segmBPoint2.y())) {
            return null;
        }

        // Share an extremity and are collinear
        if ((almostEqual(segmAPoint1, segmBPoint1) && isPointInSegment(segmAPoint1, segmAPoint2, segmBPoint2))
                || (almostEqual(segmAPoint1, segmBPoint2) && isPointInSegment(segmAPoint1, segmAPoint2, segmBPoint1))) {
            return segmAPoint1;
        }
        if ((almostEqual(segmAPoint2, segmBPoint1) && isPointInSegment(segmAPoint2, segmAPoint1, segmBPoint2))
                || (almostEqual(segmAPoint2, segmBPoint2) && isPointInSegment(segmAPoint2, segmAPoint1, segmBPoint1))) {
            return segmAPoint2;
        }

        // The first segment, that act as line in intersection, is of zero length
        if (almostEqual(segmAPoint1, segmAPoint2)) {
            return isPointInSegment(segmAPoint1, segmBPoint1, segmBPoint2) ? segmAPoint1 : null;
        }

        // Other cases
        Coordinates lineSegmIntersection = lineSegmIntersection(segmAPoint1, segmAPoint2, segmBPoint1, segmBPoint2);
        if (lineSegmIntersection == null || !isPointInSegment(lineSegmIntersection, segmAPoint1, segmAPoint2)) {
            return null;
        }
        return lineSegmIntersection;
    }

    /**
     *
     * Verifies if the polygon is simple.
     *
     * @param polygon the polygon.
     * @return true if the polygon is simple, false if not.
     */
    public static boolean isPolygonSimple(Polygon polygon) {
        if (polygon.size() < 3) {
            return false;
        }

        try {
            for (int i = 0; i < polygon.size(); i++) {
                for (int j = i + 1; j < polygon.size(); j++) {
                    Coordinates aSource = polygon.get(i);
                    Coordinates aTarget = polygon.get((i + 1) % polygon.size());
                    Coordinates bSource = polygon.get(j);
                    Coordinates bTarget = polygon.get((j + 1) % polygon.size());
                    Coordinates intersection = segmSegmIntersection(aSource, aTarget, bSource, bTarget);

                    if (intersection == null) {
                        continue;
                    }

                    boolean aConsecutiveB = almostEqual(aTarget, intersection)
                            && almostEqual(bSource, intersection);
                    boolean bConsecutiveA = almostEqual(bTarget, intersection)
                            && almostEqual(aSource, intersection);

                    if (!aConsecutiveB && !bConsecutiveA) {
                        return false;
                    }
                }
            }
        } catch (CollinearLinesException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets the area of a simple polygon (Shoelace formula).
     *
     * @param polygon the polygon.
     * @return the area of the polygon.
     */
    public static double polygonArea(Polygon polygon) {
        return Math.abs(polygonSignedArea(polygon));
    }

    /**
     * Gets the signed area of a simple polygon (Shoelace formula). The area is
     * positive when the polygon is provided in anticlockwise order, negative
     * otherwise.
     *
     * @param polygon the polygon.
     * @return the area of the polygon.
     */
    public static double polygonSignedArea(Polygon polygon) {
        assert (isPolygonSimple(polygon)) : "The polygon must be simple.";

        double firstSum = 0.0;
        double secondSum = 0.0;
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            firstSum += previousPoint.x() * currentPoint.y();
            secondSum += currentPoint.x() * previousPoint.y();
            previousPoint = currentPoint;
        }
        return (secondSum - firstSum) / 2.0;
    }

    /**
     * Computes the perimeter of the given polygon.
     *
     * @param polygon the polygon.
     * @return the perimeter of the polygon.
     */
    public static double polygonPerimeter(Polygon polygon) {
        double perimeter = 0;
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            perimeter += Geom2D.magnitude(currentPoint.minus(previousPoint));
            previousPoint = currentPoint;
        }
        return perimeter;
    }

    /**
     * Computes the isoperimetric quotient of a polygon. The isoperimetric
     * quotient is the ratio between the area of the polygon and that of a
     * circle with equal circumference.
     *
     * @param polygon the polygon.
     * @return the polygon isoperimetric quotient.
     */
    public static double isoperimetricQuotient(Polygon polygon) {
        double perimeter = polygonPerimeter(polygon);
        double area = polygonArea(polygon);
        return 4 * Math.PI * area / (perimeter * perimeter);
    }

    /**
     * Returns the centroid of a polygon.
     *
     * @param polygon the polygon.
     * @return its centroid.
     */
    public static Coordinates polygonCentroid(Polygon polygon) {
        double x = 0;
        double y = 0;
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            double factor = polygon.get(i).y() * polygon.get(j).x() - polygon.get(i).x() * polygon.get(j).y();
            x += (polygon.get(i).x() + polygon.get(j).x()) * factor;
            y += (polygon.get(i).y() + polygon.get(j).y()) * factor;
        }
        return (new Coordinates(x, y)).divide(6 * polygonSignedArea(polygon));
    }

    /**
     * Verifies if the point is in the polygon boundary.
     *
     * @param point the point.
     * @param polygon the polygon.
     * @return true if point is in the polygon boundary, false otherwise.
     */
    public static boolean isPointInPolygonBoundary(Coordinates point, Polygon polygon) {
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            if (isPointInSegment(point, previousPoint, currentPoint)) {
                return true;
            }
            previousPoint = currentPoint;
        }
        return false;
    }

    /**
     * Verifies if the point is inside a polygon.
     *
     * @param point the point.
     * @param polygon the polygon.
     * @return true if the point is in the polygon or its boundary, false
     * otherwise.
     */
    public static boolean isPointInPolygon(Coordinates point, Polygon polygon) {
        if (isPointInPolygonBoundary(point, polygon)) {
            return true;
        }

        try {
            double x = Collections.max(polygon, new Coordinates.LeftMostComparator()).x() + GeomXD.randomDouble(10, 20);
            double y = Collections.max(polygon, new Coordinates.BottomMostComparator()).y() + GeomXD.randomDouble(10, 20);
            Coordinates farAwayPoint = new Coordinates(x, y);

            int crossingsCount = 0;
            Coordinates previousPoint = polygon.get(polygon.size() - 1);
            for (Coordinates currentPoint : polygon) {
                Coordinates intersection = segmSegmIntersection(point, farAwayPoint, previousPoint, currentPoint);
                if (intersection != null) {
                    boolean countingTwiceForZeroLengthEdge = almostEqual(previousPoint, currentPoint);
                    boolean countingTwiceForCrossingAtEdgeExtremity = almostEqual(intersection, previousPoint);
                    if (!countingTwiceForZeroLengthEdge && !countingTwiceForCrossingAtEdgeExtremity) {
                        crossingsCount++;
                    }
                }
                previousPoint = currentPoint;
            }
            return (crossingsCount % 2 == 1);

        } catch (CollinearLinesException e) {
            return isPointInPolygon(point, polygon);
        }
    }

    /**
     * Get the convex hull for a set of points (Graham scan).
     *
     * @param points the points.
     * @return the convex hull.
     */
    public static Polygon convexHull(List<Coordinates> points) {
        if (points.size() <= 1) {
            return new Polygon(points);
        }

        Polygon convexHull = new Polygon();
        Coordinates bottomMost = Collections.min(points, new Coordinates.BottomMostComparator());
        convexHull.add(bottomMost);

        List<Coordinates> otherPoints = new ArrayList<>();
        for (Coordinates point : points) {
            if (!Geom2D.almostEqual(bottomMost, point)) {
                otherPoints.add(point);
            }
        }

        if (otherPoints.isEmpty()) {
            return convexHull;
        }

        Collections.sort(otherPoints, new Coordinates.AngleComparator(bottomMost));
        convexHull.add(otherPoints.get(0));

        Coordinates[] pastEdge = {convexHull.get(0), convexHull.get(convexHull.size() - 1)};
        for (Coordinates point : otherPoints) {
            if (!almostEqual(point, pastEdge[1])) {
                while (getTurn(pastEdge, point) == ConvexHullTurn.Right) {
                    convexHull.remove(convexHull.size() - 1);
                    pastEdge[0] = convexHull.get(Math.max(convexHull.size() - 2, 0));
                    pastEdge[1] = convexHull.get(convexHull.size() - 1);
                }
                if (getTurn(pastEdge, point) == ConvexHullTurn.Collinear) {
                    convexHull.remove(convexHull.size() - 1);
                }
                convexHull.add(point);
                pastEdge[0] = convexHull.get(convexHull.size() - 2);
                pastEdge[1] = convexHull.get(convexHull.size() - 1);
            }
        }
        return convexHull;
    }

    /**
     * Indicate the kind of turn performed when considering three points
     * according to the Graham scan order.
     */
    private enum ConvexHullTurn {

        Left,
        Right,
        Collinear
    }

    /**
     * Detects the kind of turn performed considering the last convex hull edge
     * previously inserted and the new point to be evaluated.
     *
     * @param edge the last edge inserted in the convex hull.
     * @param point the new point to be evaluated.
     * @return the kind of turn.
     */
    private static ConvexHullTurn getTurn(Coordinates[] edge, Coordinates point) {
        double crossProductZ = (edge[1].x() - edge[0].x()) * (point.y() - edge[0].y())
                - (edge[1].y() - edge[0].y()) * (point.x() - edge[0].x());

        if (GeomXD.almostEqual(crossProductZ, 0.0)) {
            return ConvexHullTurn.Collinear;
        }

        return crossProductZ > 0 ? ConvexHullTurn.Left : ConvexHullTurn.Right;
    }
}
