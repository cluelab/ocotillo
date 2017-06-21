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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a quadrant for a bucket grid. A bucket can only be referred with
 * positive indexes.
 *
 * @param <E> the type of elements contained.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BucketQuadrant<E> {

    /**
     * The buckets in the quadrant.
     */
    private Set[][] buckets = new HashSet[0][0];

    /**
     * Constructs a bucket quadrant.
     */
    public BucketQuadrant() {
        ensureCapacity(100);
    }

    /**
     * Gets the elements in the bucket with given indexes.
     *
     * @param i the x index.
     * @param j the y index.
     * @return the elements in bucket (i,j).
     */
    public Collection<E> get(int i, int j) {
        assert (i >= 0 && j >= 0) : "Negative indexes";
        if (i >= buckets.length || j >= buckets[i].length) {
            return new HashSet<>();
        } else {
            return Collections.unmodifiableCollection(buckets[i][j]);
        }
    }

    /**
     * Adds the elements to the bucket with given indexes.
     *
     * @param elements the elements.
     * @param i the x index.
     * @param j the y index.
     */
    public void addAll(Collection<E> elements, int i, int j) {
        assert (i >= 0 && j >= 0) : "Negative indexes";
        ensureCapacity(Math.max(i, j));
        buckets[i][j].addAll(elements);
    }

    /**
     * Removes the elements from the bucket with given indexes.
     *
     * @param elements the elements.
     * @param i the x index.
     * @param j the y index.
     */
    public void removeAll(Collection<E> elements, int i, int j) {
        assert (i >= 0 && j >= 0) : "Negative indexes";
        if (i < buckets.length && j < buckets[i].length) {
            buckets[i][j].removeAll(elements);
        }
    }

    /**
     * Removes the elements from the quadrant.
     *
     * @param element the element.
     */
    public void remove(E element) {
        for (Set[] bucketRow : buckets) {
            for (Set bucket : bucketRow) {
                bucket.remove(element);
            }
        }
    }

    /**
     * Clears the quadrant.
     */
    public void clear() {
        for (Set[] bucketRow : buckets) {
            for (Set bucket : bucketRow) {
                bucket.clear();
            }
        }
    }

    /**
     * Ensures that the data structure can fit a bucket with given indexes.
     *
     * @param i the x index.
     * @param j the y index.
     */
    private void ensureCapacity(int size) {
        if (size >= buckets.length) {
            int factor = size / Math.max(1, buckets.length) + 1;
            int newSize = (buckets.length + 1) * factor;
            Set[][] newBuckets = new Set[newSize][newSize];

            for (int i = 0; i < newBuckets.length; i++) {
                for (int j = 0; j < newBuckets.length; j++) {
                    if (i < buckets.length && j < buckets.length) {
                        newBuckets[i][j] = buckets[i][j];
                    } else {
                        newBuckets[i][j] = new HashSet();
                    }
                }
            }

            buckets = newBuckets;
        }
    }

}
