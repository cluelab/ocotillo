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

import ocotillo.graph.Node;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class BucketGridTest {

    @Test
    public void testAddGet() throws Exception {
        BucketGrid<Node> grid = new BucketGrid<>();
        Node nodeA = new Node("a");
        Node nodeB = new Node("b");
        Node nodeC = new Node("c");

        grid.addAll(Arrays.asList(nodeA, nodeB), -1, -1, -2, -1);
        grid.add(nodeC, -1, -2);
        assertThat(grid.get(-1, -1).size(), is(2));
        assertThat(grid.get(-1, -1), hasItem(nodeA));
        assertThat(grid.get(-1, -1), hasItem(nodeB));
        assertThat(grid.get(-1, -2).size(), is(3));
        assertThat(grid.get(-1, -1, -2, -1), hasItem(nodeA));
        assertThat(grid.get(-1, -1, -2, -1), hasItem(nodeB));
        assertThat(grid.get(-1, -1, -2, -1), hasItem(nodeC));

        grid.addAll(Arrays.asList(nodeA, nodeB), 2, -3);
        assertThat(grid.get(2, -3), hasItem(nodeA));
        assertThat(grid.get(2, -3), hasItem(nodeB));

        assertThat(grid.get(4, 6), is(empty()));
    }

    @Test
    public void testRemove() throws Exception {
        BucketGrid<Node> grid = new BucketGrid<>();
        Node nodeA = new Node("a");
        Node nodeB = new Node("b");
        Node nodeC = new Node("c");

        grid.add(nodeC, 2, 2, 0, 0);
        grid.addAll(Arrays.asList(nodeA, nodeB), 2, 3, 0, 0);
        assertThat(grid.get(2, 0).size(), is(3));
        assertThat(grid.get(2, 0), hasItem(nodeA));
        assertThat(grid.get(2, 0), hasItem(nodeB));
        assertThat(grid.get(2, 0), hasItem(nodeC));
        assertThat(grid.get(3, 0).size(), is(2));
        assertThat(grid.get(3, 0), hasItem(nodeA));
        assertThat(grid.get(3, 0), hasItem(nodeB));

        grid.remove(nodeB);
        assertThat(grid.get(2, 0), not(hasItem(nodeB)));
        assertThat(grid.get(3, 0), not(hasItem(nodeB)));

        grid.removeAll(Arrays.asList(nodeA, nodeB));
        assertThat(grid.get(2, 0), hasItem(nodeC));
        assertThat(grid.get(3, 0), is(empty()));
    }
}
