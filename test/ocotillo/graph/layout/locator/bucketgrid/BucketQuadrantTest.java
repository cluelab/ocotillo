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

public class BucketQuadrantTest {

    @Test
    public void testAddGet() throws Exception {
        BucketQuadrant<Node> quadrant = new BucketQuadrant<>();        
        Node nodeA = new Node("a");
        Node nodeB = new Node("b");

        quadrant.addAll(Arrays.asList(nodeA, nodeB), 0, 0);
        assertThat(quadrant.get(0, 0), hasItem(nodeA));
        assertThat(quadrant.get(0, 0), hasItem(nodeB));

        quadrant.addAll(Arrays.asList(nodeA, nodeB), 2, 3);
        assertThat(quadrant.get(2, 3), hasItem(nodeA));
        assertThat(quadrant.get(2, 3), hasItem(nodeB));
    
        assertThat(quadrant.get(4, 6), is(empty()));
    }

    @Test
    public void testRemove() throws Exception {
        BucketQuadrant<Node> quadrant = new BucketQuadrant<>();        
        Node nodeA = new Node("a");
        Node nodeB = new Node("b");

        quadrant.addAll(Arrays.asList(nodeA, nodeB), 2, 3);
        assertThat(quadrant.get(2, 3), hasItem(nodeA));
        assertThat(quadrant.get(2, 3), hasItem(nodeB));
    
        quadrant.removeAll(Arrays.asList(nodeB), 2, 3);
        assertThat(quadrant.get(2, 3), hasItem(nodeA));
        assertThat(quadrant.get(2, 3), not(hasItem(nodeB)));
        
        quadrant.removeAll(Arrays.asList(nodeB), 4, 6);
        assertThat(quadrant.get(4, 6), is(empty()));
        
    }
}
