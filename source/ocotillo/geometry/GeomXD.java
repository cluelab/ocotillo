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

import java.util.Random;

/**
 * Collects the geometry methods that operates on vectorial spaces of arbitrary
 * dimensions.
 */
public class GeomXD {

    /**
     * The default tolerance value.
     */
    public static final double epsilon = 0.0001;

    private static final Random randomGen = new Random();

    /**
     * Determines if two numbers are almost equal.
     *
     * @param a the first number.
     * @param b the second number.
     * @return true if they are almost equal, false if not.
     */
    public static boolean almostEqual(double a, double b) {
        return almostEqual(a, b, epsilon);
    }

    /**
     * Determines if two numbers are almost equal.
     *
     * @param a the first number.
     * @param b the second number.
     * @param tolerance The tolerance.
     * @return true if they are almost equal, false if not.
     */
    public static boolean almostEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    /**
     * Determines if two coordinates are almost equal. The comparison is
     * performed over all dimensions. If the coordinates have vector different
     * number of dimensions, returns false.
     *
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @return true if almost equal, false if not.
     */
    public static boolean almostEqual(Coordinates a, Coordinates b) {
        return almostEqual(a, b, epsilon);
    }

    /**
     * Determines if two coordinates are almost equal. The comparison is
     * performed over all dimensions. If the coordinates have vector different
     * number of dimensions, returns false.
     *
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @param tolerance the tolerance.
     * @return true if almost equal, false if not.
     */
    public static boolean almostEqual(Coordinates a, Coordinates b, double tolerance) {
        if (a.dim() != b.dim()) {
            return false;
        }

        for (int i = 0; i < a.dim(); ++i) {
            if (!almostEqual(a.get(i), b.get(i), tolerance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the magnitude of a vector.
     *
     * @param vector the coordinates of a vector.
     * @return the vector's magnitude.
     */
    public static double magnitude(Coordinates vector) {
        double sum = 0;
        for (int i = 0; i < vector.dim(); ++i) {
            sum += vector.get(i) * vector.get(i);
        }
        return Math.sqrt(sum);
    }

    /**
     * Generates a new random value.
     *
     * @param maxValue the maximum value.
     * @return a random double.
     */
    public static double randomDouble(double maxValue) {
        return randomGen.nextDouble() * maxValue;
    }

    /**
     * Generates a new random value.
     *
     * @param minValue the minimum value.
     * @param maxValue the maximum value.
     * @return a random double.
     */
    public static double randomDouble(double minValue, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("The minimum value provided is greater than the maximum value.");
        }
        return randomDouble(maxValue - minValue) + minValue;
    }

    /**
     * Transforms an angle expressed in radians into degrees.
     *
     * @param radians the angle in radians to convert.
     * @return the angle in degrees.
     */
    public static double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

    /**
     * Transforms an angle expressed in degrees into radians.
     *
     * @param degrees the angle in degrees to convert.
     * @return the angle in radians.
     */
    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    /**
     * Transforms an angle expressed in radians in the equivalent angle in the
     * interval [-PI, PI).
     *
     * @param angleInRadians the angle in radians.
     * @return the equivalent angle in radians.
     */
    public static double normalizeRadiansAngle(double angleInRadians) {
        double normalisedAngle = posNormalizeRadiansAngle(angleInRadians);
        if (normalisedAngle >= Math.PI) {
            normalisedAngle -= 2 * Math.PI;
        }
        return normalisedAngle;
    }

    /**
     * Transforms an angle expressed in degrees in the equivalent angle in the
     * interval [-180, 180).
     *
     * @param angleInDegrees the angle in degrees.
     * @return the equivalent angle in degrees.
     */
    public static double normalizeDegreesAngle(double angleInDegrees) {
        double normalisedAngle = posNormalizeDegreesAngle(angleInDegrees);
        if (normalisedAngle >= 180.0) {
            normalisedAngle -= 360.0;
        }
        return normalisedAngle;
    }

    /**
     * Transforms an angle expressed in radians in the equivalent angle in the
     * interval [0, 2PI).
     *
     * @param angleInRadians the angle in radians.
     * @return the equivalent angle in radians.
     */
    public static double posNormalizeRadiansAngle(double angleInRadians) {
        double completeRoundsToRemove = Math.floor(angleInRadians / (2.0 * Math.PI));
        return angleInRadians - 2.0 * Math.PI * completeRoundsToRemove;
    }

    /**
     * Transforms an angle expressed in degrees in the equivalent angle in the
     * interval [0, 360°).
     *
     * @param angleInDegrees The angle in degrees.
     * @return the equivalent angle in degrees.
     */
    public static double posNormalizeDegreesAngle(double angleInDegrees) {
        double completeRoundsToRemove = Math.floor(angleInDegrees / 360);
        return angleInDegrees - 360 * completeRoundsToRemove;
    }

    /**
     * Computes the difference between two angles in radians. The difference is
     * in the range [0,PI).
     *
     * @param firstAngleInRadians the first angle.
     * @param secondAngleInRadians the second angle.
     * @return the angle difference.
     */
    public static double angleDiff(double firstAngleInRadians, double secondAngleInRadians) {
        return Math.abs(normalizeRadiansAngle(firstAngleInRadians - secondAngleInRadians));
    }

    /**
     * Computes the midpoint of a segment.
     *
     * @param a the first point of the segment.
     * @param b the second point of the segment.
     * @return the midpoint.
     */
    public static Coordinates midPoint(Coordinates a, Coordinates b) {
        Coordinates result = new Coordinates(Math.max(a.dim(), b.dim()));
        result.plusIP(a);
        result.plusIP(b);
        result.divideIP(2.0);
        return result;
    }

    /**
     * Computes a point in between two other ones. The point is computed as
     * (b-a)*ratio+a, therefore for ratio=0 returns a, for ratio=1 returns b,
     * and the result will be really in between the two points only for ratios
     * in (0,1).
     *
     * @param a the first point.
     * @param b the second point.
     * @param ratio the ratio of the distance a-b to consider.
     * @return the in between point at the desired distance from a.
     */
    public static Coordinates inBetweenPoint(Coordinates a, Coordinates b, double ratio) {
        Coordinates result = new Coordinates(Math.max(a.dim(), b.dim()));
        result.plusIP(b);
        result.minusIP(a);
        result.timesIP(ratio);
        result.plusIP(a);
        return result;
    }
}
