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
 * Observer for graph modifications.
 */
public abstract class GraphObserver implements Observer {

    private final Graph graphObserved;

    /**
     * Constructs an element attribute observer.
     *
     * @param graphObserved the observed graph.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GraphObserver(Graph graphObserved) {
        this.graphObserved = graphObserved;
        graphObserved.registerObserver(this);
    }

    /**
     * Updates the elements recently been added or removed.
     *
     * @param changedElements the elements that changed.
     */
    public abstract void updateElements(Collection<Element> changedElements);

    /**
     * Updates the sub-graphs that recently been added or removed.
     *
     * @param changedSubGraphs the sub-graphs that changed.
     */
    public abstract void updateSubGraphs(Collection<Graph> changedSubGraphs);

    /**
     * Updates the attributes that have recently been added or removed. The
     * observers will be notified only for the local attributes added or
     * removed, and not for changes in inherited attributes.
     *
     * @param changedAttributes the attributes that changed.
     */
    public abstract void updateAttributes(Collection<Attribute<?>> changedAttributes);

    @Override
    public void unregister() {
        graphObserved.unregisterObserver(this);
    }
}
