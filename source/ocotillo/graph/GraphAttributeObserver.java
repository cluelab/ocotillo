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
 * Observer for graph attribute modifications.
 */
public abstract class GraphAttributeObserver implements Observer {

    private final GraphAttribute<?> attributeObserved;

    /**
     * Constructs an element attribute observer.
     *
     * @param attributeObserved the observed attribute.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GraphAttributeObserver(GraphAttribute<?> attributeObserved) {
        this.attributeObserved = attributeObserved;
        attributeObserved.registerObserver(this);
    }

    /**
     * Updates the attribute to its current value.
     */
    public abstract void update();

    @Override
    public void unregister() {
        attributeObserved.unregisterObserver(this);
    }

}
