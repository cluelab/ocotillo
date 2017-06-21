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

import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GeomXDTest {

    @Test
    public void testArealmostEqualWithDoubles() {
        assertThat(GeomXD.almostEqual(0, 0), is(true));
        assertThat(GeomXD.almostEqual(10, 10), is(true));
        assertThat(GeomXD.almostEqual(10.000000001, 9.99999999), is(true));
        assertThat(GeomXD.almostEqual(10, 9.9, 0.1), is(true));

        assertThat(GeomXD.almostEqual(001, 00011), is(false));
        assertThat(GeomXD.almostEqual(10, 8.9, 1), is(false));
    }

    @Test
    public void testArealmostEqualWithCoord() {
        Coordinates a = new Coordinates(0, 0.0000001);
        Coordinates b = new Coordinates(0.00001, -0.00001);
        assertThat(GeomXD.almostEqual(a, b), is(true));

        a = new Coordinates(72, 34);
        b = new Coordinates(71.99993, 34.00001);
        assertThat(GeomXD.almostEqual(a, b), is(true));
    }

    @Test
    public void testRandomDouble() {
        int[] buckets = new int[10];
        for (int i = 0; i < 200; i++) {
            double random = GeomXD.randomDouble(100);
            assertThat(random, greaterThanOrEqualTo(0.0));
            assertThat(random, lessThanOrEqualTo(100.0));
            buckets[(int) random / 10] += 1;
        }

        for (int bucket : buckets) {
            assertThat(bucket, greaterThan(0));
        }

        buckets = new int[10];
        for (int i = 0; i < 200; i++) {
            double random = GeomXD.randomDouble(50, 100);
            assertThat(random, greaterThanOrEqualTo(50.0));
            assertThat(random, lessThanOrEqualTo(100.0));
            buckets[(int) random / 10] += 1;
        }

        for (int i = 0; i < 5; ++i) {
            assertThat(buckets[i], is(0));
        }
        for (int i = 5; i < 10; ++i) {
            assertThat(buckets[i], greaterThan(0));
        }
    }

    @Test
    public void testRadiansToDegrees() {
        assertThat(GeomXD.radiansToDegrees(1), isAlmost(57.2957795131));
        assertThat(GeomXD.radiansToDegrees(Math.PI / 2), isAlmost(90));
        assertThat(GeomXD.radiansToDegrees(Math.PI), isAlmost(180));
        assertThat(GeomXD.radiansToDegrees(Math.PI * 2), isAlmost(360));
        assertThat(GeomXD.radiansToDegrees(2.63), isAlmost(150.687900119));
    }

    @Test
    public void testDegreesToRadians() {
        assertThat(GeomXD.degreesToRadians(1), isAlmost(0.0174532925));
        assertThat(GeomXD.degreesToRadians(90), isAlmost(Math.PI / 2));
        assertThat(GeomXD.degreesToRadians(180), isAlmost(Math.PI));
        assertThat(GeomXD.degreesToRadians(360), isAlmost(Math.PI * 2));
        assertThat(GeomXD.degreesToRadians(150.687900119), isAlmost(2.63));
    }

    @Test
    public void testNormalizeRadiansAngle() {
        assertThat(GeomXD.normalizeRadiansAngle(0), isAlmost(0));
        assertThat(GeomXD.normalizeRadiansAngle(Math.PI), isAlmost(-Math.PI));
        assertThat(GeomXD.normalizeRadiansAngle(2 * Math.PI), isAlmost(0));
        assertThat(GeomXD.normalizeRadiansAngle(1), isAlmost(1));
        assertThat(GeomXD.normalizeRadiansAngle(1 - 2 * Math.PI), isAlmost(1));
        assertThat(GeomXD.normalizeRadiansAngle(1 - 6 * Math.PI), isAlmost(1));
        assertThat(GeomXD.normalizeRadiansAngle(-1 + 2 * Math.PI), isAlmost(-1));
        assertThat(GeomXD.normalizeRadiansAngle(-1), isAlmost(-1));
        assertThat(GeomXD.normalizeRadiansAngle(-1 - 6 * Math.PI), isAlmost(-1));
    }

    @Test
    public void testNormalizeDegreesAngle() {
        assertThat(GeomXD.normalizeDegreesAngle(0), isAlmost(0));
        assertThat(GeomXD.normalizeDegreesAngle(180), isAlmost(-180));
        assertThat(GeomXD.normalizeDegreesAngle(360), isAlmost(0));
        assertThat(GeomXD.normalizeDegreesAngle(1), isAlmost(1));
        assertThat(GeomXD.normalizeDegreesAngle(361), isAlmost(1));
        assertThat(GeomXD.normalizeDegreesAngle(721), isAlmost(1));
        assertThat(GeomXD.normalizeDegreesAngle(359), isAlmost(-1));
        assertThat(GeomXD.normalizeDegreesAngle(-1), isAlmost(-1));
        assertThat(GeomXD.normalizeDegreesAngle(-361), isAlmost(-1));
    }

    @Test
    public void testPosNormalizeRadiansAngle() {
        assertThat(GeomXD.posNormalizeRadiansAngle(0), isAlmost(0));
        assertThat(GeomXD.posNormalizeRadiansAngle(Math.PI), isAlmost(Math.PI));
        assertThat(GeomXD.posNormalizeRadiansAngle(2 * Math.PI), isAlmost(0));
        assertThat(GeomXD.posNormalizeRadiansAngle(1), isAlmost(1));
        assertThat(GeomXD.posNormalizeRadiansAngle(1 - 2 * Math.PI), isAlmost(1));
        assertThat(GeomXD.posNormalizeRadiansAngle(1 - 6 * Math.PI), isAlmost(1));
        assertThat(GeomXD.posNormalizeRadiansAngle(-1), isAlmost(2 * Math.PI - 1));
        assertThat(GeomXD.posNormalizeRadiansAngle(-1 - 6 * Math.PI), isAlmost(2 * Math.PI - 1));
    }

    @Test
    public void testPosNormalizeDegreesAngle() {
        assertThat(GeomXD.posNormalizeDegreesAngle(0), isAlmost(0));
        assertThat(GeomXD.posNormalizeDegreesAngle(180), isAlmost(180));
        assertThat(GeomXD.posNormalizeDegreesAngle(360), isAlmost(0));
        assertThat(GeomXD.posNormalizeDegreesAngle(1), isAlmost(1));
        assertThat(GeomXD.posNormalizeDegreesAngle(361), isAlmost(1));
        assertThat(GeomXD.posNormalizeDegreesAngle(721), isAlmost(1));
        assertThat(GeomXD.posNormalizeDegreesAngle(-1), isAlmost(359));
        assertThat(GeomXD.posNormalizeDegreesAngle(-361), isAlmost(359));
    }

    @Test
    public void testMidPoint() {
        assertThat(GeomXD.midPoint(new Coordinates(0, 0), new Coordinates(10, 10)), is(new Coordinates(5, 5)));
        assertThat(GeomXD.midPoint(new Coordinates(2, 3, 10), new Coordinates(8, 5, 4)), is(new Coordinates(5, 4, 7)));
        assertThat(GeomXD.midPoint(new Coordinates(2, 3), new Coordinates(8, 5, 4)), is(new Coordinates(5, 4, 2)));
    }

    @Test
    public void testInBetweenPoint() {
        assertThat(GeomXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0), is(new Coordinates(0, 0)));
        assertThat(GeomXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 1), is(new Coordinates(10, 10)));
        assertThat(GeomXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0.5), is(new Coordinates(5, 5)));
        assertThat(GeomXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0.4), is(new Coordinates(4, 4)));
    }
}
