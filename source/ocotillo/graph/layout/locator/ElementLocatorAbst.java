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
package ocotillo.graph.layout.locator;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ElementLocatorAbst implements ElementLocator {

    protected Graph graph;
    protected NodeAttribute<Coordinates> nodePositions;
    protected NodeAttribute<Coordinates> nodeSizes;
    protected EdgeAttribute<Double> edgeWidths;
    protected EdgeAttribute<ControlPoints> edgePoints;
    protected NodeAttribute<Boolean> nodesToConsider;
    protected NodeAttribute<Boolean> nodesToExclude;
    protected EdgeAttribute<Boolean> edgesToConsider;
    protected EdgeAttribute<Boolean> edgesToExclude;

    @Override
    public Collection<Node> getCloseNodes(Edge edge, double radius) {
        List<Coordinates> polyline = new ArrayList<>(2);
        polyline.add(nodePositions.get(edge.source()));
        polyline.add(nodePositions.get(edge.target()));
        return getCloseNodes(polyline, radius);
    }

    @Override
    public Collection<Edge> getCloseEdges(Edge edge, double radius) {
        List<Coordinates> polyline = new ArrayList<>(2);
        polyline.add(nodePositions.get(edge.source()));
        polyline.add(nodePositions.get(edge.target()));
        Collection<Edge> closeEdges = getCloseEdges(polyline, radius);
        closeEdges.remove(edge);
        return closeEdges;
    }

    /**
     * Indicates whether the given node should be considered.
     *
     * @param node the node.
     * @return true is it will be considered, false otherwise.
     */
    protected boolean shouldWeConsider(Node node) {
        return graph.has(node) && ((nodesToConsider == null && nodesToExclude == null)
                || (nodesToConsider != null && nodesToConsider.get(node))
                || (nodesToExclude != null && !nodesToExclude.get(node)));
    }

    /**
     * Indicates whether the given edge should be considered.
     *
     * @param edge the edge.
     * @return true is it will be considered, false otherwise.
     */
    protected boolean shouldWeConsider(Edge edge) {
        return graph.has(edge) && ((edgesToConsider == null && edgesToExclude == null)
                || (edgesToConsider != null && edgesToConsider.get(edge))
                || (edgesToExclude != null && !edgesToExclude.get(edge)));
    }

}
