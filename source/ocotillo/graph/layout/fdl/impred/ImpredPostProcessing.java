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

import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom2D;
import ocotillo.geometry.Polygon;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser;
import ocotillo.graph.layout.Layout2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Post processing step for the ImPrEd algorithm.
 */
public abstract class ImpredPostProcessing extends ImpredElement {

    /**
     * Executes this post-processing computation.
     */
    protected abstract void execute();

    /**
     * Expands or contracts the flexible edges in the graph.
     */
    public static class FlexibleEdges extends ImpredPostProcessing {

        protected final Collection<Edge> flexibleEdges;
        protected final double contractDistance;
        protected final double expandDistance;
        public int refreshInterval = 1;
        public double shutDownTemperature = 0.2;
        private int refreshCounter = 0;

        /**
         * Constructs a flexible edges post processing.
         *
         * @param flexibleEdges the attribute indicating the flexible edges.
         * @param contractDistance the distance at which a flexible chain is
         * contracted.
         * @param expandDistance the distance at which a flexible segment is
         * expanded.
         */
        public FlexibleEdges(Collection<Edge> flexibleEdges, double contractDistance, double expandDistance) {
            this.flexibleEdges = flexibleEdges;
            this.contractDistance = contractDistance;
            this.expandDistance = expandDistance;
        }

        @Override
        protected void execute() {
            if (refreshCounter % refreshInterval == 0 && temperature() > shutDownTemperature) {
                expandFlexibleEdges();
                contractFlexibleEdges();
                refreshCounter++;
            }
        }

        /**
         * Expands the flexible segments whose length exceed the expand
         * distance.
         */
        private void expandFlexibleEdges() {
            for (Edge flexibleEdge : flexibleEdges) {
                BendExplicitGraphSynchroniser.MirrorEdge mirrorEdge = synchronizer().getMirrorEdge(flexibleEdge);
                for (Edge segment : new ArrayList<>(mirrorEdge.segments())) {
                    if (Layout2D.edgeLength(segment, mirrorPositions(), null, null) > expandDistance) {
                        synchronizer().addMirrorBend(mirrorEdge, segment);
                    }
                }
            }
        }

        /**
         * Contracts the flexible chains n1,e1,n2,e2,n3 where n1 and n3 are
         * closer than the contract distance and no other nodes is in the
         * triangle n1,n2,n3.
         */
        private void contractFlexibleEdges() {
            for (Edge flexibleEdge : flexibleEdges) {
                BendExplicitGraphSynchroniser.MirrorEdge mirrorEdge = synchronizer().getMirrorEdge(flexibleEdge);
                for (Node bend : new ArrayList<>(mirrorEdge.bends())) {
                    Node n1 = mirrorGraph().inEdges(bend).iterator().next().source();
                    Node n3 = mirrorGraph().outEdges(bend).iterator().next().target();
                    Coordinates n1Pos = mirrorPositions().get(n1);
                    Coordinates n3Pos = mirrorPositions().get(n3);
                    double distance = Geom2D.magnitude(n1Pos.minus(n3Pos));
                    if (distance < contractDistance && !nodesInTriangle(n1, bend, n3)) {
                        synchronizer().removeMirrorBend(mirrorEdge, bend);
                    }
                }
            }
        }

        /**
         * Verifies if there are unrelated nodes inside the given triangle.
         *
         * @param n1 the first triangle node.
         * @param n2 the second triangle node.
         * @param n3 the third triangle node.
         * @return true if there are nodes inside, false otherwise.
         */
        private boolean nodesInTriangle(Node n1, Node n2, Node n3) {
            Coordinates n1Pos = mirrorPositions().get(n1);
            Coordinates n2Pos = mirrorPositions().get(n2);
            Coordinates n3Pos = mirrorPositions().get(n3);
            Box nodeBox = Box.boundingBox(Arrays.asList(n1Pos, n2Pos, n3Pos), 1);
            Collection<Node> closeNodes = locator().getNodesPartiallyInBox(nodeBox);
            closeNodes.remove(n1);
            closeNodes.remove(n2);
            closeNodes.remove(n3);
            Polygon triangle = new Polygon(Arrays.asList(n1Pos, n2Pos, n3Pos));
            for (Node unrelatedNode : closeNodes) {
                if (Geom2D.isPointInPolygon(mirrorPositions().get(unrelatedNode), triangle)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Delays the iteration if the minim iteration time indicated is not yet
     * passed.
     */
    public static class MinIterationTime extends ImpredPostProcessing {

        protected final long minIterationTime;
        private long iterationStartTime;

        /**
         * Constructs a minimum iteration time ImPrEd post-processing.
         *
         * @param timeInMilliseconds the iteration time in milliseconds.
         */
        public MinIterationTime(long timeInMilliseconds) {
            this.minIterationTime = timeInMilliseconds;
        }

        @Override
        protected void execute() {
            long endIterationTime = System.nanoTime();
            long iterationTime = (endIterationTime - iterationStartTime) / 1000000;
            if (iterationTime < minIterationTime) {
                try {
                    Thread.sleep(minIterationTime - iterationTime);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            iterationStartTime = System.nanoTime();
        }

    }

}
