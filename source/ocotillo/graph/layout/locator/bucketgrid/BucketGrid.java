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
package ocotillo.graph.layout.locator.bucketgrid;

import ocotillo.graph.Element;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;

/**
 * A grid of buckets. Buckets can have both positive and negative indexes.
 *
 * @param <E> the type of elements contained.
 */
public class BucketGrid<E extends Element> {

    private final BucketQuadrant<E> plusPlus = new BucketQuadrant<>();
    private final BucketQuadrant<E> plusMinus = new BucketQuadrant<>();
    private final BucketQuadrant<E> minusPlus = new BucketQuadrant<>();
    private final BucketQuadrant<E> minusMinus = new BucketQuadrant<>();

    /**
     * Gets the elements contained in the bucket with given indexes.
     *
     * @param i the x index.
     * @param j the y index.
     * @return the elements in the bucket (i,j).
     */
    public Collection<E> get(int i, int j) {
        MachineCoordConverter converter = new MachineCoordConverter();
        converter.computeFor(i, j);
        return converter.quadrant.get(converter.i, converter.j);
    }

    /**
     * Gets the elements contained in the buckets identified by two ranges of
     * indexes.
     *
     * @param i1 the smaller x index.
     * @param i2 the bigger x index.
     * @param j1 the smaller y index.
     * @param j2 the bigger y index.
     * @return the elements in the buckets with given ranges.
     */
    public Collection<E> get(int i1, int i2, int j1, int j2) {
        assert (i1 <= i2 && j1 <= j2) : "The first index should be lesser or equal to the second";
        Set<E> elements = new HashSet<>();
        for (int i = i1; i <= i2; i++) {
            for (int j = j1; j <= j2; j++) {
                elements.addAll(get(i, j));
            }
        }
        return elements;
    }

    /**
     * Adds an element to the given bucket.
     *
     * @param element the element.
     * @param i the x index.
     * @param j the y index.
     */
    public void add(E element, int i, int j) {
        addAll(Arrays.asList(element), i, i, j, j);
    }

    /**
     * Add an element in the buckets identified by two ranges of indexes.
     *
     * @param element the element.
     * @param i1 the smaller x index.
     * @param i2 the bigger x index.
     * @param j1 the smaller y index.
     * @param j2 the bigger y index.
     */
    public void add(E element, int i1, int i2, int j1, int j2) {
        addAll(Arrays.asList(element), i1, i2, j1, j2);
    }

    /**
     * Adds a collection of elements to the given bucket.
     *
     * @param elements the elements.
     * @param i the x index.
     * @param j the y index.
     */
    public void addAll(Collection<E> elements, int i, int j) {
        addAll(elements, i, i, j, j);
    }

    /**
     * Add a collection of elements in the buckets identified by two ranges of
     * indexes.
     *
     * @param elements the elements.
     * @param i1 the smaller x index.
     * @param i2 the bigger x index.
     * @param j1 the smaller y index.
     * @param j2 the bigger y index.
     */
    public void addAll(Collection<E> elements, int i1, int i2, int j1, int j2) {
        assert (i1 <= i2 && j1 <= j2) : "The first index should be lesser or equal to the second";
        MachineCoordConverter converter = new MachineCoordConverter();
        for (int i = i1; i <= i2; i++) {
            for (int j = j1; j <= j2; j++) {
                converter.computeFor(i, j);
                converter.quadrant.addAll(elements, converter.i, converter.j);
            }
        }
    }

    /**
     * Removes an element from the grid.
     *
     * @param element the element.
     */
    public void remove(E element) {
        plusPlus.remove(element);
        plusMinus.remove(element);
        minusPlus.remove(element);
        minusMinus.remove(element);
    }

    /**
     * Removes all the given elements from the grid.
     *
     * @param elements the elements.
     */
    public void removeAll(Collection<E> elements) {
        for (E elmement : elements) {
            remove(elmement);
        }
    }

    /**
     * Clears the grid.
     */
    public void clear() {
        plusPlus.clear();
        plusMinus.clear();
        minusPlus.clear();
        minusMinus.clear();
    }

    /**
     * The coordinates of a bucket according the machine indexes.
     */
    @EqualsAndHashCode
    @SuppressWarnings("unchecked")
    private class MachineCoordConverter {

        private BucketQuadrant<E> quadrant;
        private int i;
        private int j;

        /**
         * Identifies the machine coordinates given the logical ones. Logical
         * indexes can be positive or negative, which is not supported by the
         * machine. Therefore, the logical coordinates are converted in machine
         * ones with include the quadrant to use and the new indexes. When using
         * negative logical indexes, the indexes are opposed and translated by
         * one to use the position 0, which would be unused otherwise.
         *
         * @param i
         * @param j
         */
        private void computeFor(int i, int j){
            if (i >= 0 && j >= 0) {
                quadrant = plusPlus;
            } else if (i >= 0 && j < 0) {
                quadrant = plusMinus;
            } else if (i < 0 && j >= 0) {
                quadrant = minusPlus;
            } else {
                quadrant = minusMinus;
            }

            this.i = i >= 0 ? i : -i - 1;
            this.j = j >= 0 ? j : -j - 1;
        }

    }

}
