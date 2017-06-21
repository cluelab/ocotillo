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

import java.util.Comparator;

/**
 * The coordinates of a point in a vectorial space.
 */
public class Coordinates {

    private Double[] coordinates;

    /**
     * Constructs 2D coordinates.
     *
     * @param x the first coordinate.
     * @param y the second coordinate.
     */
    public Coordinates(double x, double y) {
        coordinates = new Double[2];
        coordinates[0] = x;
        coordinates[1] = y;
    }

    /**
     * Constructs 3D coordinates.
     *
     * @param x the first coordinate.
     * @param y the second coordinate.
     * @param z the third coordinate.
     */
    public Coordinates(double x, double y, double z) {
        coordinates = new Double[3];
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
    }

    /**
     * Constructs coordinates of arbitrary dimension. The dimension must be at
     * least 2.
     *
     * @param dim the coordinates dimension.
     */
    public Coordinates(int dim) {
        if (dim < 2) {
            throw new IllegalArgumentException("The dimension of a Coordinates instance must be al least 2.");
        }
        coordinates = new Double[dim];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = 0.0;
        }
    }

    /**
     * Copies an existing coordinates instance.
     *
     * @param otherCoordinates the existing instance.
     */
    public Coordinates(Coordinates otherCoordinates) {
        coordinates = new Double[otherCoordinates.dim()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = otherCoordinates.get(i);
        }
    }

    /**
     * Returns the first coordinate.
     *
     * @return the first coordinate.
     */
    public double x() {
        return coordinates[0];
    }

    /**
     * Returns the second coordinate.
     *
     * @return the second coordinate.
     */
    public double y() {
        return coordinates[1];
    }

    /**
     * Returns the third coordinate.
     *
     * @return the third coordinate, or zero if the coordinate dimension is less
     * than 3.
     */
    public double z() {
        if (coordinates.length >= 3) {
            return coordinates[2];
        }
        return 0;
    }

    /**
     * Sets the first coordinate.
     *
     * @param x the first coordinate.
     */
    public void setX(double x) {
        coordinates[0] = x;
    }

    /**
     * Sets the second coordinate.
     *
     * @param y the second coordinate.
     */
    public void setY(double y) {
        coordinates[1] = y;
    }

    /**
     * Sets the third coordinate. The method has no effect if the dimension is
     * less than 3.
     *
     * @param z the third coordinate.
     */
    public void setZ(double z) {
        if (coordinates.length >= 3) {
            coordinates[2] = z;
        }
    }

    /**
     * Gets the coordinate with given index.
     *
     * @param i the coordinate index.
     * @return the coordinate at given index, or zero if the dimension is less
     * or equal to the index.
     */
    public double get(int i) {
        if (i < coordinates.length) {
            return coordinates[i];
        }
        return 0;
    }

    /**
     * Sets the coordinate with given index.
     *
     * @param i the coordinate index.
     * @param value the value to assign.
     */
    public void setAt(int i, double value) {
        if (i < coordinates.length) {
            coordinates[i] = value;
        }
    }

    /**
     * Sets the coordinate with the given values.
     *
     * @param values the values to assign.
     */
    public void set(double... values) {
        for(int i = 0; i < Math.min(values.length, coordinates.length); i++){
            setAt(i, values[i]);
        }
    }

    /**
     * Resets all the coordinates to zero.
     */
    public void reset() {
        for(int i = 0; i < coordinates.length; i++){
            coordinates[i] = 0.0;
        }
    }

    /**
     * Returns the dimension of the coordinates.
     *
     * @return the dimension of the coordinates.
     */
    public int dim() {
        return coordinates.length;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (int i = 0; i < dim(); i++) {
            hash += 83 * i * (new Double(get(i))).hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinates other = (Coordinates) obj;
        for (int i = 0; i < Math.max(dim(), other.dim()); i++) {
            if (get(i) != other.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String answer = "(";
        String divider = ", ";
        for (int i = 0; i < dim(); i++) {
            answer += get(i) + divider;
        }
        return answer.substring(0, answer.length() - divider.length()) + ")";
    }

    /**
     * Computes the opposite of a vector (a -> -a). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter.
     *
     * @return the opposite of this vector.
     */
    public Coordinates minus() {
        return minusComp(new Coordinates(dim()));
    }

    /**
     * Computes the opposite of a vector (a -> -a) in place. This instance is
     * modified to contain the new value.
     *
     * @return the opposite of this vector.
     */
    public Coordinates minusIP() {
        return minusComp(this);
    }

    private Coordinates minusComp(Coordinates result) {
        for (int i = 0; i < dim(); i++) {
            result.setAt(i, -get(i));
        }
        return result;
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter.
     *
     * @param b the second vector.
     * @return the sum of two vectors.
     */
    public Coordinates plus(Coordinates b) {
        return plusComp(b, new Coordinates(dim()));
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b) in place. This instance is
     * modified to contain the new value.
     *
     * @param b the second vector.
     * @return the sum of the two vectors.
     */
    public Coordinates plusIP(Coordinates b) {
        return plusComp(b, this);
    }

    private Coordinates plusComp(Coordinates b, Coordinates result) {
        for (int i = 0; i < Math.min(dim(), b.dim()); i++) {
            result.setAt(i, get(i) + b.get(i));
        }
        return result;
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b). This instance is not
     * modified.
     *
     * @param b the second vector.
     * @return the difference of the two vectors.
     */
    public Coordinates minus(Coordinates b) {
        return minusComp(b, new Coordinates(dim()));
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b) in place. This
     * instance is modified to contain the new value.
     *
     * @param b the second vector.
     * @return the difference of the two vectors.
     */
    public Coordinates minusIP(Coordinates b) {
        return minusComp(b, this);
    }

    private Coordinates minusComp(Coordinates b, Coordinates result) {
        for (int i = 0; i < Math.min(dim(), b.dim()); i++) {
            result.setAt(i, get(i) - b.get(i));
        }
        return result;
    }

    /**
     * Computes the scalar product of this vector (a,s -> a*s). This instance is
     * not modified.
     *
     * @param scalar the scalar factor.
     * @return the scalar product of this vector.
     */
    public Coordinates times(double scalar) {
        return timesComp(scalar, new Coordinates(dim()));
    }

    /**
     * Computes the scalar product of of this vector (a,s -> a*s) in place. This
     * instance is modified to contain the new value.
     *
     * @param scalar the scalar factor.
     * @return the scalar product of this vector.
     */
    public Coordinates timesIP(double scalar) {
        return timesComp(scalar, this);
    }

    private Coordinates timesComp(double scalar, Coordinates result) {
        for (int i = 0; i < dim(); i++) {
            result.setAt(i, get(i) * scalar);
        }
        return result;
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s). This instance
     * is not modified.
     *
     * @param scalar the scalar factor.
     * @return the scalar division of this vector.
     */
    public Coordinates divide(double scalar) {
        return divideComp(scalar, new Coordinates(dim()));
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s) in place. This
     * instance is modified to contain the new value.
     *
     * @param scalar the scalar factor.
     * @return the scalar division of this vector.
     */
    public Coordinates divideIP(double scalar) {
        return divideComp(scalar, this);
    }

    private Coordinates divideComp(double scalar, Coordinates result) {
        for (int i = 0; i < dim(); i++) {
            result.setAt(i, get(i) / scalar);
        }
        return result;
    }

    /**
     * Orders Coordinates from left to right.
     */
    public static class LeftMostComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(a.x(), b.x());
        }
    }

    /**
     * Orders Coordinates from bottom to top.
     */
    public static class BottomMostComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(a.y(), b.y());
        }
    }

    /**
     * Orders Coordinates from lowest to highest magnitude.
     */
    public static class MagnitudeComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(GeomXD.magnitude(a), GeomXD.magnitude(b));
        }
    }

    /**
     * Orders Coordinates according to increasing angles.
     */
    public static class AngleComparator implements Comparator<Coordinates> {

        private Coordinates reference = new Coordinates(0.0, 0.0);

        public AngleComparator() {
        }

        public AngleComparator(Coordinates customReference) {
            reference = customReference;
        }

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(Geom2D.angle(a.minus(reference)), Geom2D.angle(b.minus(reference)));
        }
    }
}
