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

import java.util.Collection;

/**
 * Observer for an element attribute.
 *
 * @param <K> The type of element handled.
 */
public abstract class ElementAttributeObserver<K extends Element> implements Observer {

    private final ElementAttribute<K, ?> attributeObserved;

    /**
     * Constructs an element attribute observer.
     *
     * @param attributeObserved the observed attribute.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ElementAttributeObserver(ElementAttribute<K, ?> attributeObserved) {
        this.attributeObserved = attributeObserved;
        attributeObserved.registerObserver(this);
    }

    /**
     * Updates the elements whose attribute recently changed.
     *
     * @param changedElements the elements whose attributes changed.
     */
    public abstract void update(Collection<K> changedElements);

    /**
     * Updated all elements of the graph. This is used when the elements that
     * changed are not directly known by the attribute, such as when the default
     * value is changed.
     */
    public abstract void updateAll();

    @Override
    public void unregister() {
        attributeObserved.unregisterObserver(this);
    }
}
