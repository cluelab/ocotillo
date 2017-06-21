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

import ocotillo.geometry.Coordinates;
import org.junit.Test;

public class StdAttributeTest {

    @Test
    public void testCheckCorrectCompatibility() {
        NodeAttribute<Coordinates> attribute = new NodeAttribute<>(new Coordinates(0, 2));
        StdAttribute.checkStdAttributeCompatibility(StdAttribute.nodePosition.name(), attribute);
    }

    @Test(expected = ClassCastException.class)
    public void testCheckWrongCompatibility() {
        NodeAttribute<Double> attribute = new NodeAttribute<>(2.0);
        StdAttribute.checkStdAttributeCompatibility(StdAttribute.nodePosition.name(), attribute);
    }

}
