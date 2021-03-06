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

/**
 * Edge attribute.
 *
 * @param <V> the type of value accepted.
 */
public class EdgeAttribute<V> extends ElementAttribute<Edge, V> {

    /**
     * Constructs an edge attribute.
     *
     * @param defaultValue the value of an edge when not directly set.
     */
    public EdgeAttribute(V defaultValue) {
        super(defaultValue);
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.edge;
    }

}
