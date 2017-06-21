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
package ocotillo.graph.layout.fdl.impred;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser;
import ocotillo.graph.layout.locator.ElementLocator;

public class ImpredElement {

    private Impred impred;

    /**
     * Attaches the ImPrEd element to an ImPrEd instance. Should be only called
     * by ImpredBuilder.
     *
     * @param impred the ImPrEd instance.
     */
    protected void attachTo(Impred impred) {
        assert (this.impred == null) : "The ImPrEd element was already attached to an ImPrEd instance.";
        assert (impred != null) : "Attaching the ImPrEd element to a null impred instance.";
        this.impred = impred;
    }

    /**
     * Returns the mirror graph to be used to compute the ImPrEd elements.
     *
     * @return the mirror graph.
     */
    protected final Graph mirrorGraph() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.mirrorGraph;
    }

    /**
     * Returns the node positions for the mirror graph.
     *
     * @return the node positions.
     */
    protected final NodeAttribute<Coordinates> mirrorPositions() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.mirrorPositions;
    }

    /**
     * Returns the synchronizer that acts on the mirror graph.
     *
     * @return the synchronizer.
     */
    protected final BendExplicitGraphSynchroniser synchronizer() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.synchronizer;
    }

    /**
     * Returns the locator that acts on the mirror graph.
     *
     * @return the locator.
     */
    protected final ElementLocator locator() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.locator;
    }

    /**
     * Returns the current temperature.
     *
     * @return the temperature.
     */
    protected final double temperature() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.thermostat.temperature;
    }

    /**
     * Returns the current forces.
     *
     * @return the forces.
     */
    protected final NodeAttribute<Coordinates> forces() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.forces;
    }

    /**
     * Returns the current constraints.
     *
     * @return the constraints.
     */
    protected final NodeAttribute<Double> constraints() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.constraints;
    }
    
    /**
     * Returns the current movements.
     *
     * @return the movements.
     */
    protected final NodeAttribute<Coordinates> movements() {
        assert (impred != null) : "The ImPrEd element has not been attached yet.";
        return impred.movements;
    }
}
