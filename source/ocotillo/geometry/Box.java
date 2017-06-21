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

import java.util.Arrays;
import java.util.Collection;
import lombok.EqualsAndHashCode;

/**
 * A 2D box.
 */
@EqualsAndHashCode
public class Box {

    public final double bottom;
    public final double left;
    public final double top;
    public final double right;

    /**
     * Constructs a box.
     *
     * @param bottom the bottom most y value.
     * @param left the left most x value.
     * @param top the top most y value.
     * @param right the right most x value.
     */
    public Box(double bottom, double left, double top, double right) {
        this.bottom = bottom;
        this.left = left;
        this.top = top;
        this.right = right;
    }

    /**
     * Constructs a box.
     *
     * @param center the box center.
     * @param size the box size.
     */
    public Box(Coordinates center, Coordinates size) {
        this.bottom = center.y() - size.y() / 2;
        this.left = center.x() - size.x() / 2;
        this.top = center.y() + size.y() / 2;
        this.right = center.x() + size.x() / 2;
    }

    /**
     * Gets the coordinates of the bottom left corner.
     *
     * @return the bottom left corner.
     */
    public Coordinates bottomLeft() {
        return new Coordinates(left, bottom);
    }

    /**
     * Gets the coordinates of the top left corner.
     *
     * @return the top left corner.
     */
    public Coordinates topLeft() {
        return new Coordinates(left, top);
    }

    /**
     * Gets the coordinates of the bottom right corner.
     *
     * @return the bottom right corner.
     */
    public Coordinates bottomRight() {
        return new Coordinates(right, bottom);
    }

    /**
     * Gets the coordinates of the top right corner.
     *
     * @return the top right corner.
     */
    public Coordinates topRight() {
        return new Coordinates(right, top);
    }

    /**
     * Gets the coordinates of the center of the box.
     *
     * @return the center.
     */
    public Coordinates center() {
        return new Coordinates((left + right) / 2, (bottom + top) / 2);
    }

    /**
     * Gets the dimensions of the box in coordinate form.
     *
     * @return the box dimensions.
     */
    public Coordinates size() {
        return new Coordinates(width(), height());
    }

    /**
     * Computes the width of the box.
     *
     * @return the width.
     */
    public double width() {
        return right - left;
    }

    /**
     * Computes the height of the box.
     *
     * @return the height.
     */
    public double height() {
        return top - bottom;
    }

    /**
     * Returns the largest of the two box dimensions.
     *
     * @return the largest dimension.
     */
    public double maxDim() {
        return Math.max(width(), height());
    }

    /**
     * Returns the smallest of the two box dimensions.
     *
     * @return the smallest dimension.
     */
    public double minDim() {
        return Math.min(width(), height());
    }

    /**
     * Scales a box by a given factor.
     *
     * @param factors the scaling factors.
     * @return the resulting box.
     */
    public Box scale(Coordinates factors) {
        return new Box(bottom * factors.y(), left * factors.x(), top * factors.y(), right * factors.x());
    }

    /**
     * Expands a box by the given margins.
     *
     * @param margins the margins.
     * @return the resulting box.
     */
    public Box expand(Coordinates margins) {
        return new Box(bottom - margins.y(), left - margins.x(), top + margins.y(), right + margins.x());
    }

    /**
     * Shifts a box by a given vector.
     *
     * @param movement the movement vector.
     * @return the resulting box.
     */
    public Box shift(Coordinates movement) {
        return new Box(bottom + movement.y(), left + movement.x(), top + movement.y(), right + movement.x());
    }

    /**
     * Combines this box with another one.
     *
     * @param box the other box.
     * @return the combination of the boxes.
     */
    public Box combine(Box box) {
        return combine(Arrays.asList(this, box));
    }

    /**
     * Computes the intersection of this box with another one.
     *
     * @param box the other box.
     * @return null if they do not intersect, the intersection box otherwise.
     */
    public Box intersect(Box box) {
        return intersect(Arrays.asList(this, box));
    }

    @Override
    public String toString() {
        return "[b=" + bottom + ", l=" + left + ", t=" + top + ", r=" + right + "]";
    }

    /**
     * Verifies if a point is inside a box.
     *
     * @param point the point.
     * @return true if the point is in the box, false otherwise.
     */
    public boolean contains(Coordinates point) {
        return left <= point.x() && point.x() <= right
                && bottom <= point.y() && point.y() <= top;
    }

    /**
     * Verifies if a collection of points are all inside a box.
     *
     * @param points the points.
     * @return true if all the point are in the box, false otherwise.
     */
    public boolean contains(Collection<Coordinates> points) {
        for (Coordinates point : points) {
            if (!contains(point)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the bounding box of the points.
     *
     * @param points the points.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points) {
        return boundingBox(points, 0.0);
    }

    /**
     * Returns the bounding box of the points, inserting a margin around them.
     *
     * @param points the points.
     * @param margin the margin to consider around each point.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points, double margin) {
        return boundingBox(points, margin, margin);
    }

    /**
     * Returns the bounding box of the points, inserting a margin around them.
     *
     * @param points the points.
     * @param xMargin the horizontal margin to consider around each point.
     * @param yMargin the vertical margin to consider around each point.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points, double xMargin, double yMargin) {
        double bottom = Double.POSITIVE_INFINITY;
        double left = Double.POSITIVE_INFINITY;
        double top = Double.NEGATIVE_INFINITY;
        double right = Double.NEGATIVE_INFINITY;
        for (Coordinates point : points) {
            left = Math.min(left, point.x() - xMargin);
            bottom = Math.min(bottom, point.y() - yMargin);
            right = Math.max(right, point.x() + xMargin);
            top = Math.max(top, point.y() + yMargin);
        }
        return new Box(bottom, left, top, right);
    }

    /**
     * Combine multiple boxes into one.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static Box combine(Collection<Box> boxes) {
        double bottom = Double.POSITIVE_INFINITY;
        double left = Double.POSITIVE_INFINITY;
        double top = Double.NEGATIVE_INFINITY;
        double right = Double.NEGATIVE_INFINITY;
        for (Box box : boxes) {
            left = Math.min(left, box.left);
            bottom = Math.min(bottom, box.bottom);
            right = Math.max(right, box.right);
            top = Math.max(top, box.top);
        }
        return new Box(bottom, left, top, right);
    }

    /**
     * Computes the intersection of multiple boxes.
     *
     * @param boxes the boxes.
     * @return null if the boxes do not have a region shared by all of them, the
     * intersection box otherwise.
     */
    public static Box intersect(Collection<Box> boxes) {
        double bottom = Double.NEGATIVE_INFINITY;
        double left = Double.NEGATIVE_INFINITY;
        double top = Double.POSITIVE_INFINITY;
        double right = Double.POSITIVE_INFINITY;
        for (Box box : boxes) {
            left = Math.max(left, box.left);
            bottom = Math.max(bottom, box.bottom);
            right = Math.min(right, box.right);
            top = Math.min(top, box.top);
        }
        if (left <= right && bottom <= top) {
            return new Box(bottom, left, top, right);
        } else {
            return null;
        }
    }

}
