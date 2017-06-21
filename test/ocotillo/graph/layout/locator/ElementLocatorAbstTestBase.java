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
package ocotillo.graph.layout.locator;

import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Base test class, not instantiable")
public abstract class ElementLocatorAbstTestBase extends ElementLocatorTestBase {

    @Override
    protected abstract ElementLocatorAbst createInstance();

    protected abstract ElementLocatorAbst createInstance(NodeAttribute<Boolean> nodesToConsider, NodeAttribute<Boolean> nodesToExclude);

    @Test
    public void shouldWeConsider() {
        Node a = graph.newNode();
        NodeAttribute<Boolean> nodesToConsider = graph.newNodeAttribute("nodesToConsider", false);
        NodeAttribute<Boolean> nodesToExclude = graph.newNodeAttribute("nodesToExclude", false);

        ElementLocatorAbst locator = createInstance();
        assertThat(locator.shouldWeConsider(a), is(true));

        locator = createInstance(nodesToConsider, null);
        assertThat(locator.shouldWeConsider(a), is(false));

        nodesToConsider.set(a, true);
        locator = createInstance(nodesToConsider, null);
        assertThat(locator.shouldWeConsider(a), is(true));

        locator = createInstance(null, nodesToExclude);
        assertThat(locator.shouldWeConsider(a), is(true));

        nodesToExclude.set(a, true);
        locator = createInstance(null, nodesToExclude);
        assertThat(locator.shouldWeConsider(a), is(false));
    }
}
