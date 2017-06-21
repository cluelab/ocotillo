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
import ocotillo.geometry.GeomXD;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Constrain for the ImPrEd algorithm.
 */
public abstract class ImpredConstraint extends ImpredElement {

    /**
     * Computes the movement constraints for all the nodes in the graph.
     *
     * @return the computed constraints.
     */
    protected abstract NodeAttribute<Double> computeConstraints();

    /**
     * Constraint that gradually decreases the global max movement for that the
     * nodes can perform at each iteration.
     */
    public static class DecreasingMaxMovement extends ImpredConstraint {

        /**
         * The global max movement at the beginning of the computation.
         */
        protected double initialMaxMovement;

        /**
         * Constructs a decreasing max movement constraint.
         *
         * @param initialMaxMovement the max movement at the beginning of the
         * computation.
         */
        public DecreasingMaxMovement(double initialMaxMovement) {
            this.initialMaxMovement = initialMaxMovement;
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            return new NodeAttribute<>(initialMaxMovement * temperature());
        }

    }

    /**
     * Constraint that consents large movements only if the force direction is
     * consistent in successive iterations.
     */
    public static class MovementAcceleration extends ImpredConstraint {

        /**
         * The max movement allowed.
         */
        protected double maxMovement;
        protected Map<Node, Double> previousAngles = new HashMap<>();
        protected Map<Node, Double> previousMovements = new HashMap<>();

        /**
         * Constructs a decreasing max movement constraint.
         *
         * @param maxMovement the max movement.
         */
        public MovementAcceleration(double maxMovement) {
            this.maxMovement = maxMovement;
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> constraints = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : mirrorGraph().nodes()) {

                Coordinates currentForce = forces().get(node);
                double currentAngle = Geom2D.angle(currentForce);

                if (Geom2D.almostEqual(currentForce, new Coordinates(0, 0))) {
                    previousAngles.remove(node);
                    previousMovements.put(node, 0.0);
                    continue;
                }

                double currentMovement;
                if (!previousAngles.containsKey(node)) {
                    currentMovement = maxMovement / 5;
                } else {
                    double previousAngle = previousAngles.get(node);
                    double angleDiff = GeomXD.angleDiff(currentAngle, previousAngle);
                    if (angleDiff < Math.PI / 3) {
                        currentMovement = Math.min(previousMovements.get(node) * (1 + 2 * ( 1 - angleDiff / (Math.PI/3))), maxMovement);
                    } else if (angleDiff < Math.PI / 2) {
                        currentMovement = previousMovements.get(node);
                    } else {
                        currentMovement = previousMovements.get(node) / (1 + 4 * (angleDiff / (Math.PI / 2) - 1) );
                    }
                }
                constraints.set(node, currentMovement);
                previousMovements.put(node, currentMovement);
                previousAngles.put(node, currentAngle);
            }
            return constraints;
        }

    }

    /**
     * Constraint that limit the node movement so that each node does not cross
     * its surrounding edges.
     */
    public static class SurroundingEdges extends ImpredConstraint {

        /**
         * The surrounding edges for each node.
         */
        protected NodeAttribute<Collection<Edge>> surroundingEdges;

        /**
         * Construct a surrounding edges constraint.
         *
         * @param surroundingEdges the surrounding edges.
         */
        public SurroundingEdges(NodeAttribute<Collection<Edge>> surroundingEdges) {
            this.surroundingEdges = surroundingEdges;
        }

        /**
         * Returns the affected nodes.
         *
         * @return the affected nodes.
         */
        protected Collection<Node> nodes() {
            return mirrorGraph().nodes();
        }

        /**
         * Returns the edges to involve in the computation for a given node.
         *
         * @param node the given node.
         * @return the edges to check.
         */
        protected Collection<Edge> edges(Node node) {
            Collection<Edge> edges = new HashSet<>();
            for (Edge originalSurroundingEdge : surroundingEdges.get(node)) {
                edges.addAll(synchronizer().getMirrorEdge(originalSurroundingEdge).segments());
            }
            pruneDistantEdges(edges, node);
            edges.removeAll(mirrorGraph().inEdges(node));
            edges.removeAll(mirrorGraph().outEdges(node));
            return edges;
        }

        /**
         * Removes from a edge collection the edges that are too far from the
         * given node.
         *
         * @param edges the edge collection to be pruned.
         * @param node the given node.
         */
        protected void pruneDistantEdges(Collection<Edge> edges, Node node) {
            if (constraints().getDefault() != Double.POSITIVE_INFINITY) {
                double distanceToConsider = 3 * constraints().getDefault();
                edges.retainAll(locator().getCloseEdges(node, distanceToConsider));
            }
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> maxMovement = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : nodes()) {
                for (Edge edge : edges(node)) {
                    if (!edge.isNodeExtremity(node)) {
                        Coordinates nPos = mirrorPositions().get(node);
                        Coordinates sPos = mirrorPositions().get(edge.source());
                        Coordinates tPos = mirrorPositions().get(edge.target());
                        Coordinates projection = Geom2D.pointOnLineProjection(nPos, sPos, tPos);
                        if (Geom2D.isPointInSegment(projection, sPos, tPos)) {
                            computeConstraintsProjInside(node, edge, projection, maxMovement);
                        } else {
                            computeConstraintsProjOutside(node, edge, maxMovement);
                        }
                    }
                }
            }
            return maxMovement;
        }

        /**
         * Computes the constraints when the node projection is inside the edge.
         *
         * @param node the node.
         * @param edge the edge.
         * @param projection the node projection on the edge.
         * @param maxMovement the max movements.
         */
        protected void computeConstraintsProjInside(Node node, Edge edge, Coordinates projection, NodeAttribute<Double> maxMovement) {
            Coordinates pn = projection.minus(mirrorPositions().get(node));
            double nodeCollisionAngle = Geom2D.angle(pn);
            double edgeCollisionAngle = nodeCollisionAngle + Math.PI;
            double collisionDistance = Geom2D.magnitude(pn) / 2;
            reduceMovement(node, nodeCollisionAngle, collisionDistance, maxMovement);
            reduceMovement(edge.source(), edgeCollisionAngle, collisionDistance, maxMovement);
            reduceMovement(edge.target(), edgeCollisionAngle, collisionDistance, maxMovement);
        }

        /**
         * Computes the constraints when the node projection is outside the
         * edge.
         *
         * @param node the node.
         * @param edge the edge.
         * @param maxMovement the max movements.
         */
        protected void computeConstraintsProjOutside(Node node, Edge edge, NodeAttribute<Double> maxMovement) {
            Coordinates nodePos = mirrorPositions().get(node);
            Coordinates sourcePos = mirrorPositions().get(edge.source());
            Coordinates targetPos = mirrorPositions().get(edge.target());

            Node closeExtremity, farExtremity;
            if (Geom2D.magnitude(sourcePos.minus(nodePos)) < Geom2D.magnitude(targetPos.minus(nodePos))) {
                closeExtremity = edge.source();
                farExtremity = edge.target();
            } else {
                closeExtremity = edge.target();
                farExtremity = edge.source();
            }

            Coordinates closePos = mirrorPositions().get(closeExtremity);
            Coordinates farPos = mirrorPositions().get(farExtremity);
            double nodeAngle = Geom2D.angle(closePos.minus(nodePos));
            double edgeAngle = Geom2D.posNormalizeRadiansAngle(nodeAngle + Math.PI);

            double axisAngle = nodeAngle + Math.PI / 2;
            Coordinates axisPointA = nodePos.plus(closePos.minus(nodePos).divide(2));
            Coordinates axisPointB = Geom2D.unitVector(axisAngle).plusIP(axisPointA);

            double nodeCollDist = Geom2D.magnitude(closePos.minus(nodePos)) / 2;
            double closeExtrCollDist = nodeCollDist;
            double farExtrCollDist = Geom2D.pointToLineDistance(farPos, axisPointA, axisPointB);

            reduceMovement(node, nodeAngle, nodeCollDist, maxMovement);
            reduceMovement(closeExtremity, edgeAngle, closeExtrCollDist, maxMovement);
            reduceMovement(farExtremity, edgeAngle, farExtrCollDist, maxMovement);
        }

        /**
         * Reduces the node max movement.
         *
         * @param node the node.
         * @param collisionAngle the angle of most direct collision.
         * @param collisionDistance the collision distance when moving on the
         * collision angle.
         * @param maxMovement the max movements.
         */
        protected void reduceMovement(Node node, double collisionAngle, double collisionDistance, NodeAttribute<Double> maxMovement) {
            double forceAngle = Geom2D.angle(forces().get(node));
            double forceCollAngle = Math.abs(Geom2D.normalizeRadiansAngle(forceAngle - collisionAngle));
            if (forceCollAngle < Math.PI / 2.0) {
                maxMovement.set(node, Math.min(maxMovement.get(node), collisionDistance / Math.cos(forceCollAngle)));
            }
        }

    }

    /**
     * Constraint that forbids certain nodes to move.
     */
    public static class PinnedNodes extends ImpredConstraint {

        /**
         * The pinned nodes.
         */
        protected NodeAttribute<Boolean> pinnedNodes;

        /**
         * Constructs a pinned nodes constraint.
         *
         * @param pinnedNodes the pinned nodes.
         */
        public PinnedNodes(NodeAttribute<Boolean> pinnedNodes) {
            this.pinnedNodes = pinnedNodes;
        }

        /**
         * Constructs a pinned nodes constraint.
         *
         * @param pinnedNodes the pinned nodes.
         */
        public PinnedNodes(Collection<Node> pinnedNodes) {
            this.pinnedNodes = new NodeAttribute<>(false);
            for (Node node : pinnedNodes) {
                this.pinnedNodes.set(node, true);
            }
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> maxMovement = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : mirrorGraph().nodes()) {
                if (pinnedNodes.get(node)) {
                    maxMovement.set(node, 0.0);
                }
            }
            return maxMovement;
        }

    }

}
