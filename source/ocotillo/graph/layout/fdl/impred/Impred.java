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
import ocotillo.geometry.Geom2D;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.BegsBuilder;
import ocotillo.graph.layout.locator.ElementLocator;
import ocotillo.graph.layout.locator.bucketgrid.BucketGridLocator.BglBuilder;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A force directed algorithm that supports multiple forces and constraints.
 */
public class Impred {

    protected final Graph originalGraph;
    protected final Graph mirrorGraph;
    protected final NodeAttribute<Coordinates> mirrorPositions;
    protected final NodeAttribute<Coordinates> mirrorSizes;
    protected final BendExplicitGraphSynchroniser synchronizer;
    protected final ElementLocator locator;
    protected final ImpredThermostat thermostat;

    protected final NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
    protected final NodeAttribute<Double> constraints = new NodeAttribute<>(Double.POSITIVE_INFINITY);
    protected final NodeAttribute<Coordinates> movements = new NodeAttribute<>(new Coordinates(0, 0));

    private final Collection<ImpredForce> forceSystem;
    private final Collection<ImpredConstraint> constraintSystem;
    private final Collection<ImpredPreMovement> preMovementSteps;
    private final Collection<ImpredPostProcessing> postProcessingSteps;

    private final double safetyMovementFactor = 0.9;

    /**
     * A builder for ImPrEd instances.
     */
    public static class ImpredBuilder {

        private final Graph graph;
        private NodeAttribute<Coordinates> positions;
        private EdgeAttribute<ControlPoints> bends;
        private ImpredThermostat thermostat = new ImpredThermostat.LinearCoolDown();
        private final Collection<ImpredForce> forces = new ArrayList<>();
        private final Collection<ImpredConstraint> constraints = new ArrayList<>();
        private final Collection<ImpredPreMovement> preMovements = new ArrayList<>();
        private final Collection<ImpredPostProcessing> postProcessings = new ArrayList<>();

        /**
         * Constructs an ImPrEd builder.
         *
         * @param graph the graph to be used.
         */
        public ImpredBuilder(Graph graph) {
            this.graph = graph;
        }

        /**
         * Indicates the positions to be used for the graph.
         *
         * @param positions the nodes positions.
         * @return the builder.
         */
        public ImpredBuilder withNodePositions(NodeAttribute<Coordinates> positions) {
            this.positions = positions;
            return this;
        }

        /**
         * Indicates the bends to be used for the graph.
         *
         * @param bends the edge bends.
         * @return the builder.
         */
        public ImpredBuilder withEdgeBends(EdgeAttribute<ControlPoints> bends) {
            this.bends = bends;
            return this;
        }

        /**
         * Indicates the temperature controller to be used.
         *
         * @param thermostat the desired temperature controller.
         * @return the builder.
         */
        public ImpredBuilder withThermostat(ImpredThermostat thermostat) {
            this.thermostat = thermostat;
            return this;
        }

        /**
         * Inserts the given force in the ImPrEd force system.
         *
         * @param force the force.
         * @return the builder.
         */
        public ImpredBuilder withForce(ImpredForce force) {
            forces.add(force);
            return this;
        }

        /**
         * Inserts the given constraint in the ImPrEd constraint system.
         *
         * @param constraint the constraint.
         * @return the builder.
         */
        public ImpredBuilder withConstraint(ImpredConstraint constraint) {
            constraints.add(constraint);
            return this;
        }

        /**
         * Inserts the given pre-movement step in the ImPrEd algorithm.
         *
         * @param preMovement the pre-movement step.
         * @return the builder.
         */
        public ImpredBuilder withPreMovmement(ImpredPreMovement preMovement) {
            preMovements.add(preMovement);
            return this;
        }

        /**
         * Inserts the given post-processing step in the ImPrEd algorithm.
         *
         * @param postProcessing the post-processing step.
         * @return the builder.
         */
        public ImpredBuilder withPostProcessing(ImpredPostProcessing postProcessing) {
            postProcessings.add(postProcessing);
            return this;
        }

        /**
         * Builds the ImPrEd instance.
         *
         * @return the ImPrEd instance.
         */
        public Impred build() {
            positions = positions != null ? positions : graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
            bends = bends != null ? bends : graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints);
            Impred impred = new Impred(graph, positions, bends, thermostat, forces, constraints, preMovements, postProcessings);

            thermostat.attachTo(impred);
            
            for (ImpredForce force : forces) {
                force.attachTo(impred);
            }
            for (ImpredConstraint constraint : constraints) {
                constraint.attachTo(impred);
            }
            for (ImpredPreMovement preMovement : preMovements) {
                preMovement.attachTo(impred);
            }
            for (ImpredPostProcessing postProcessing : postProcessings) {
                postProcessing.attachTo(impred);
            }
            return impred;
        }
    }

    /**
     * Constructs an ImPrEd instance.
     *
     * @param originalGraph the original graph.
     * @param positions the node positions.
     * @param bends the edge bends.
     * @param forces the force system.
     * @param constraints the constraint system.
     */
    private Impred(Graph originalGraph, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> bends, ImpredThermostat thermostat, Collection<ImpredForce> forces, Collection<ImpredConstraint> constraints, Collection<ImpredPreMovement> preMovements, Collection<ImpredPostProcessing> postProcessings) {
        if (!originalGraph.hasNodeAttribute(StdAttribute.nodeSize)) {
            originalGraph.nodeAttribute(StdAttribute.nodeSize);
        }

        this.synchronizer = new BegsBuilder(originalGraph, positions, bends)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .build();

        this.originalGraph = originalGraph;
        this.mirrorGraph = synchronizer.getMirrorGraph();
        this.mirrorPositions = synchronizer.getMirrorPositions();
        this.mirrorSizes = mirrorGraph.nodeAttribute(StdAttribute.nodeSize);

        this.locator = new BglBuilder(mirrorGraph).withNodePositions(mirrorPositions).withNodeSizes(mirrorSizes).build();
        this.thermostat = thermostat;
        this.forceSystem = forces;
        this.constraintSystem = constraints;
        this.preMovementSteps = preMovements;
        this.postProcessingSteps = postProcessings;
    }

    /**
     * Execute the ImPrEd main cycle for the given number of iterations.
     *
     * @param numberOfIterations the number of iterations.
     */
    public void iterate(int numberOfIterations) {
        synchronizer.updateMirror();
        for (int i = 0; i < numberOfIterations; i++) {

            mirrorPositions.startBulkNotification();
            forces.reset();
            constraints.reset(Double.POSITIVE_INFINITY);
            locator.rebuild();
            thermostat.updateTemperature(i, numberOfIterations);
            
            computeForces();
            computeConstraints();
            computeMovements();

            for (ImpredPreMovement preMovement : preMovementSteps) {
                preMovement.execute();
            }

            moveNodes();

            for (ImpredPostProcessing postProcessing : postProcessingSteps) {
                postProcessing.execute();
            }

            mirrorPositions.stopBulkNotification();
            synchronizer.updateOriginal();
        }
    }

    /**
     * Computes the final force for each graph node.
     *
     * @param temperature the system temperature.
     */
    private void computeForces() {
        for (ImpredForce forceDefinition : forceSystem) {
            NodeAttribute<Coordinates> computedForces = forceDefinition.computeForces();
            for (Node node : mirrorGraph.nodes()) {
                forces.set(node, computedForces.get(node).plus(forces.get(node)));
            }
        }
    }

    /**
     * Computes the final constraints for each graph node.
     *
     * @param temperature the system temperature.
     */
    private void computeConstraints() {
        for (ImpredConstraint constraintDefinition : constraintSystem) {
            NodeAttribute<Double> computedconstraint = constraintDefinition.computeConstraints();
            constraints.setDefault(Math.min(constraints.getDefault(), computedconstraint.getDefault()));
            for (Node node : mirrorGraph.nodes()) {
                double nodeMovement = Math.min(constraints.get(node), constraints.getDefault());
                nodeMovement = Math.min(nodeMovement, computedconstraint.get(node));
                constraints.set(node, nodeMovement);
            }
        }
    }

    /**
     * Computes the node movements.
     */
    private void computeMovements() {
        movements.reset();
        for (Node node : mirrorGraph.nodes()) {
            Coordinates force = forces.get(node);
            double constraint = constraints.get(node) * safetyMovementFactor;
            double magnitude = Geom2D.magnitude(force);
            if (!Geom2D.almostEqual(magnitude, 0) && !Geom2D.almostEqual(constraint, 0)) {
                Coordinates movement = new Coordinates(force);
                if (magnitude > constraint) {
                    movement.timesIP(constraint / magnitude);
                }
                movements.set(node, movement);
            }
        }
    }

    /**
     * Moves the graph nodes.
     */
    private void moveNodes() {
        for (Node node : mirrorGraph.nodes()) {
            mirrorPositions.set(node, movements.get(node).plus(mirrorPositions.get(node)));
        }
    }

    /**
     * Terminates the Impred instance.
     */
    public void close() {
        locator.close();
    }
}
