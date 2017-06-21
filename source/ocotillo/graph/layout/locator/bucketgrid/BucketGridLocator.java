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
package ocotillo.graph.layout.locator.bucketgrid;

import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Box;
import ocotillo.graph.Attribute;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Element;
import ocotillo.graph.ElementAttributeObserver;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphObserver;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Observer;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.Layout2D;
import ocotillo.graph.layout.locator.ElementLocatorAbst;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A locator that divides the space according to an homogeneously spaced grid.
 * Each cell of the grid is associated to a bucket that collects all the nodes
 * and edges partially or totally contained in that cell.
 */
public class BucketGridLocator extends ElementLocatorAbst {

    private final Set<Node> nodes = new HashSet<>();
    private final Set<Edge> edges = new HashSet<>();
    private final BucketGrid<Node> nodeGrid = new BucketGrid<>();
    private final BucketGrid<Edge> edgeGrid = new BucketGrid<>();
    private double cellSize;
    private boolean autoSync;

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Private constructor. A BucketGridLocator can only be constructed through
     * the Builder provided in order to correctly handling all the optional
     * parameters.
     */
    private BucketGridLocator() {
    }

    /**
     * Builds a BucketGridLocator.
     */
    public static class BglBuilder {

        private final Graph graph;
        private NodeAttribute<Coordinates> nodePositions;
        private NodeAttribute<Coordinates> nodeSizes;
        private EdgeAttribute<Double> edgeWidths;
        private EdgeAttribute<ControlPoints> edgePoints;
        private NodeAttribute<Boolean> nodesToConsider;
        private EdgeAttribute<Boolean> edgesToConsider;
        private NodeAttribute<Boolean> nodesToExclude;
        private EdgeAttribute<Boolean> edgesToExclude;
        private double cellSize;
        private boolean autoSync = false;

        /**
         * Construct the Builder.
         *
         * @param graph the graph.
         */
        public BglBuilder(Graph graph) {
            this.graph = graph;
        }

        /**
         * Indicates the attribute to be used to extract the nodes positions.
         *
         * @param nodePositions the position attribute.
         * @return the builder.
         */
        public BglBuilder withNodePositions(NodeAttribute<Coordinates> nodePositions) {
            this.nodePositions = nodePositions;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the nodes sizes.
         *
         * @param nodeSizes the size attribute.
         * @return the builder.
         */
        public BglBuilder withNodeSizes(NodeAttribute<Coordinates> nodeSizes) {
            this.nodeSizes = nodeSizes;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the edge widths.
         *
         * @param edgeWidths the edge widths.
         * @return the builder.
         */
        public BglBuilder withEdgeWidths(EdgeAttribute<Double> edgeWidths) {
            this.edgeWidths = edgeWidths;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the edge control
         * points.
         *
         * @param edgePoints the edge points.
         * @return the builder.
         */
        public BglBuilder withEdgePoints(EdgeAttribute<ControlPoints> edgePoints) {
            this.edgePoints = edgePoints;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the nodes to be
         * considered. The parameters "nodes to consider" and "nodes to exclude"
         * are mutually exclusive.
         *
         * @param nodesToConsider the attribute indicating which graph nodes
         * should be considered.
         * @return the builder.
         */
        public BglBuilder withNodesToConsider(NodeAttribute<Boolean> nodesToConsider) {
            this.nodesToConsider = nodesToConsider;
            this.nodesToExclude = null;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the nodes to be
         * excluded. The parameters "nodes to consider" and "nodes to exclude"
         * are mutually exclusive.
         *
         * @param nodesToExclude the attribute indicating which graph nodes
         * should be excluded.
         * @return the builder.
         */
        public BglBuilder withNodesToExclude(NodeAttribute<Boolean> nodesToExclude) {
            this.nodesToConsider = null;
            this.nodesToExclude = nodesToExclude;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the edges to be
         * considered. The parameters "edges to consider" and "edges to exclude"
         * are mutually exclusive.
         *
         * @param edgesToConsider the attribute indicating which graph edges
         * should be considered.
         * @return the builder.
         */
        public BglBuilder withEdgesToConsider(EdgeAttribute<Boolean> edgesToConsider) {
            this.edgesToConsider = edgesToConsider;
            this.edgesToExclude = null;
            return this;
        }

        /**
         * Indicates the attribute to be used to extract the edges to be
         * excluded. The parameters "edges to consider" and "edges to exclude"
         * are mutually exclusive.
         *
         * @param edgesToExclude the attribute indicating which graph edges
         * should be excluded.
         * @return the builder.
         */
        public BglBuilder withEdgesToExclude(EdgeAttribute<Boolean> edgesToExclude) {
            this.edgesToConsider = null;
            this.edgesToExclude = edgesToExclude;
            return this;
        }

        /**
         * Indicates the cell size to be used.
         *
         * @param cellSize the cell size.
         * @return the builder.
         */
        public BglBuilder withCellSize(double cellSize) {
            this.cellSize = cellSize;
            return this;
        }

        /**
         * Specifies whether the locator should observe for graph changes and
         * auto-update, or be manually updated. Manual updates might be more
         * efficient whenever the graph has completely changes at the time the
         * locator is used again.
         *
         * @param enabled indicates whether to activate or not auto-sync.
         * @return the builder.
         */
        public BglBuilder withAutoSync(boolean enabled) {
            this.autoSync = enabled;
            return this;
        }

        /**
         * Builds the BucketGridLocator using the specified parameters.
         *
         * @return the locator.
         */
        public BucketGridLocator build() {
            BucketGridLocator locator = new BucketGridLocator();
            locator.graph = graph;
            locator.nodePositions = nodePositions != null ? nodePositions : graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
            locator.nodeSizes = nodeSizes != null ? nodeSizes : graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize);
            locator.edgeWidths = edgeWidths != null ? edgeWidths : graph.<Double>edgeAttribute(StdAttribute.edgeWidth);
            locator.edgePoints = edgePoints != null ? edgePoints : graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints);
            locator.cellSize = cellSize > 0 ? cellSize : Math.max(1.0, Layout2D.graphBox(graph, locator.nodePositions, locator.nodeSizes, null, null).maxDim() / 100.0);
            locator.nodesToConsider = nodesToConsider;
            locator.nodesToExclude = nodesToExclude;
            locator.edgesToConsider = edgesToConsider;
            locator.edgesToExclude = edgesToExclude;
            locator.autoSync = autoSync;
            locator.build();
            return locator;
        }
    }

    /**
     * Builds the locator from scratch.
     */
    private void build() {
        nodes.clear();
        edges.clear();
        nodeGrid.clear();
        edgeGrid.clear();

        for (Node node : graph.nodes()) {
            if (shouldWeConsider(node)) {
                addElement(node);
            }
        }

        for (Edge edge : graph.edges()) {
            if (shouldWeConsider(edge)) {
                addElement(edge);
            }
        }

        if (autoSync) {
            addGraphObserver();
            addPositionObserver();
            addSizeObserver();
        }
    }

    /**
     * Adds a node to the locator.
     *
     * @param node the node.
     */
    private void addElement(Node node) {
        assert (!nodes.contains(node)) : "The node to insert was already in the locator";
        nodes.add(node);
        Box nodeBox = Layout2D.nodeBox(node, nodePositions, nodeSizes);
        nodeGrid.add(node, idxOf(nodeBox.left), idxOf(nodeBox.right), idxOf(nodeBox.bottom), idxOf(nodeBox.top));
    }

    /**
     * Adds an edge to the locator.
     *
     * @param edge the edge.
     */
    private void addElement(Edge edge) {
        assert (!edges.contains(edge)) : "The edge to insert was already in the locator";
        edges.add(edge);
        Box edgeBox = Layout2D.edgeBox(edge, nodePositions, edgePoints, edgeWidths);
        edgeGrid.add(edge, idxOf(edgeBox.left), idxOf(edgeBox.right), idxOf(edgeBox.bottom), idxOf(edgeBox.top));
    }

    /**
     * Removes a node from the locator, whenever it was considered before.
     *
     * @param node the node.
     */
    private void removeElement(Node node) {
        if (nodes.contains(node)) {
            nodes.remove(node);
            nodeGrid.remove(node);
        }
    }

    /**
     * Removes an edge from the locator, whenever it was considered before.
     *
     * @param edge the edge.
     */
    private void removeElement(Edge edge) {
        if (edges.contains(edge)) {
            edges.remove(edge);
            edgeGrid.remove(edge);
        }
    }

    /**
     * Updates the information on a node in the locator.
     *
     * @param node the node.
     */
    private void updateElement(Node node) {
        removeElement(node);
        if (shouldWeConsider(node)) {
            addElement(node);
        }
    }

    /**
     * Updates the information on an edge in the locator.
     *
     * @param edge the edge.
     */
    private void updateElement(Edge edge) {
        removeElement(edge);
        if (shouldWeConsider(edge)) {
            addElement(edge);
        }
    }

    /**
     * Defines and register a graph observer.
     */
    private void addGraphObserver() {
        observers.add(new GraphObserver(graph) {

            @Override
            public void updateElements(Collection<Element> changedElements) {
                for (Element element : changedElements) {
                    if (element instanceof Node) {
                        updateElement((Node) element);
                    }
                    if (element instanceof Edge) {
                        updateElement((Edge) element);
                    }
                }
            }

            @Override
            public void updateSubGraphs(Collection<Graph> changedSubGraphs) {
            }

            @Override
            public void updateAttributes(Collection<Attribute<?>> changedAttributes) {
            }
        });
    }

    /**
     * Defines and register a node position observer.
     */
    private void addPositionObserver() {
        observers.add(new ElementAttributeObserver<Node>(nodePositions) {

            @Override
            public void update(Collection<Node> changedElements) {
                for (Node node : changedElements) {
                    updateElement(node);
                    for (Edge edge : graph.inOutEdges(node)) {
                        updateElement(edge);
                    }
                }
            }

            @Override
            public void updateAll() {
                for (Node node : nodes) {
                    updateElement(node);
                }
                for (Edge edge : edges) {
                    updateElement(edge);
                }
            }
        });
    }

    /**
     * Defines and register a node size observer.
     */
    private void addSizeObserver() {
        observers.add(new ElementAttributeObserver<Node>(nodeSizes) {

            @Override
            public void update(Collection<Node> changedElements) {
                for (Node node : changedElements) {
                    updateElement(node);
                }
            }

            @Override
            public void updateAll() {
                for (Node node : nodes) {
                    updateElement(node);
                }
            }
        });
    }

    /**
     * Computes the bucket index associated to a given length.
     *
     * @param length the length.
     * @return the bucket index.
     */
    private int idxOf(double length) {
        return (int) Math.floor(length / cellSize);
    }

    /**
     * Removes the observers from the locator. This should be called when the
     * locator is no more utilized, allowing it to be destroyed.
     */
    private void removeObservers() {
        for (Observer observer : observers) {
            observer.unregister();
        }
    }

    @Override
    public void rebuild() {
        if (!autoSync) {
            build();
        }
    }

    @Override
    public void close() {
        removeObservers();
    }

    @Override
    public Collection<Node> getNodesPartiallyInBox(Box box) {
        return nodeGrid.get(idxOf(box.left), idxOf(box.right), idxOf(box.bottom), idxOf(box.top));
    }

    @Override
    public Collection<Node> getNodesFullyInBox(Box box) {
        return getNodesPartiallyInBox(box);
    }

    @Override
    public Collection<Edge> getEdgesPartiallyInBox(Box box) {
        return edgeGrid.get(idxOf(box.left), idxOf(box.right), idxOf(box.bottom), idxOf(box.top));
    }

    @Override
    public Collection<Edge> getEdgesFullyInBox(Box box) {
        return getEdgesPartiallyInBox(box);
    }

    @Override
    public Collection<Node> getCloseNodes(Coordinates point, double radius) {
        Box box = new Box(point.y() - radius, point.x() - radius, point.y() + radius, point.x() + radius);
        return getNodesPartiallyInBox(box);
    }

    @Override
    public Collection<Node> getCloseNodes(List<Coordinates> polyline, double radius) {
        return getNodesPartiallyInBox(Box.boundingBox(polyline, radius));
    }

    @Override
    public Collection<Edge> getCloseEdges(Coordinates point, double radius) {
        Box box = new Box(point.y() - radius, point.x() - radius, point.y() + radius, point.x() + radius);
        return getEdgesPartiallyInBox(box);
    }

    @Override
    public Collection<Edge> getCloseEdges(List<Coordinates> polyline, double radius) {
        return getEdgesPartiallyInBox(Box.boundingBox(polyline, radius));
    }

    @Override
    public Collection<Node> getCloseNodes(Node node, double radius) {
        Box nodeBox = Layout2D.nodeBox(node, nodePositions, nodeSizes);
        Collection<Node> closeNodes = getNodesPartiallyInBox(nodeBox.expand(new Coordinates(radius, radius)));
        closeNodes.remove(node);
        return closeNodes;
    }

    @Override
    public Collection<Edge> getCloseEdges(Node node, double radius) {
        Box nodeBox = Layout2D.nodeBox(node, nodePositions, nodeSizes);
        return getEdgesPartiallyInBox(nodeBox.expand(new Coordinates(radius, radius)));
    }

}
