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
package ocotillo.graph;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GraphAttributeTest {

    private class GraphAttrObserverTest extends GraphAttributeObserver {

        public int updateCount = 0;

        public GraphAttrObserverTest(GraphAttribute<?> attributeObserved) {
            super(attributeObserved);
        }

        public void clear() {
            updateCount = 0;
        }

        @Override
        public void update() {
            updateCount++;
        }
    }

    @Test
    public void testObservable() {
        Graph graph = new Graph();
        GraphAttribute<String> attribute = graph.newGraphAttribute("graphName", "Aldo");
        GraphAttrObserverTest observer = new GraphAttrObserverTest(attribute);

        assertThat(observer.updateCount, is(0));
        attribute.set("Mario");
        assertThat(observer.updateCount, is(1));
        attribute.set("Luigi");
        assertThat(observer.updateCount, is(2));
    }

    @Test
    public void testAttributeType() {
        Graph graph = new Graph();
        GraphAttribute<String> name = graph.newGraphAttribute("graphName", "Aldo");
        assertThat(name.getAttributeType(), is(AttributeType.graph));
    }
}
