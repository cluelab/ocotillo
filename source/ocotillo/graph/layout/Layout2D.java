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
package ocotillo.graph.layout;

import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Box;
import ocotillo.geometry.Geom2D;
import ocotillo.geometry.GeomXD;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import static ocotillo.graph.layout.LayoutXD.edgePoints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Collects several methods relevant to a 2D layout of a graph.
 */
public class Layout2D {

    /**
     * Returns the 2D length of an edge. The method use the standard attributes
     * for the node positions, the edge control points and the edge shape.
     *
     * @param edge the edge.
     * @param graph the graph.
     * @return the edge length.
     */
    public static double edgeLength(Edge edge, Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<EdgeShape> edgeShapes = graph.hasEdgeAttribute(StdAttribute.edgeShape) ? graph.<EdgeShape>edgeAttribute(StdAttribute.edgeShape) : null;
        return edgeLength(edge, positions, edgePoints, edgeShapes);

    }

    /**
     * Returns the 2D length of an edge.
     *
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgePoints the edge control points. If null, all edges have no
     * bends.
     * @param edgeShapes the edge shape. If null, all edges are poly-line.
     * @return the edge points.
     */
    public static double edgeLength(Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<StdAttribute.EdgeShape> edgeShapes) {
        List<Coordinates> points = edgePoints(edge, positions, edgePoints);
        StdAttribute.EdgeShape shape = edgeShapes != null ? edgeShapes.get(edge) : StdAttribute.EdgeShape.polyline;
        switch (shape) {
            case polyline:
                double length = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    length += Geom2D.magnitude(points.get(i).minus(points.get(i + 1)));
                }
                return length;
            default:
                throw new UnsupportedOperationException("The edge shape " + shape + " is not supported yet.");
        }
    }

    /**
     * Computes the closest edge point to a given point.
     *
     * @param graph the graph.
     * @param point the point.
     * @param edge the edge.
     * @return the closest edge point to the point passed as parameter.
     */
    public static Coordinates closestEdgePoint(Graph graph, Coordinates point, Edge edge) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<EdgeShape> edgeShapes = graph.hasEdgeAttribute(StdAttribute.edgeShape) ? graph.<EdgeShape>edgeAttribute(StdAttribute.edgeShape) : null;
        return closestEdgePoint(graph, point, edge, positions, edgePoints, edgeShapes);
    }

    /**
     * Computes the closest edge point to a given point.
     *
     * @param graph the graph.
     * @param point the point.
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgePoints the edge control points.
     * @param edgeShapes the edge shapes.
     * @return the closest edge point to the point passed as parameter.
     */
    public static Coordinates closestEdgePoint(Graph graph, Coordinates point, Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<EdgeShape> edgeShapes) {
        EdgeShape edgeShape = edgeShapes != null ? edgeShapes.get(edge) : EdgeShape.polyline;
        List<Coordinates> points = edgePoints(edge, positions, edgePoints);
        switch (edgeShape) {
            case polyline:
                double distance = Double.POSITIVE_INFINITY;
                Coordinates closestPoint = null;
                for (int i = 0; i < points.size() - 1; i++) {
                    if (!Geom2D.almostEqual(points.get(i), points.get(i + 1))) {
                        Coordinates closestPointCandidate = Geom2D.closestSegmentPoint(point, points.get(i), points.get(i + 1));
                        double candidateDistance = Geom2D.magnitude(closestPointCandidate.minus(point));
                        if (candidateDistance < distance) {
                            distance = candidateDistance;
                            closestPoint = closestPointCandidate;
                        }
                    }
                }
                return closestPoint;
            default:
                throw new UnsupportedOperationException("The edge shape " + edgeShape + " is not supported yet.");
        }
    }

    /**
     * Returns the box of a node. Uses the default attributes for position and
     * size.
     *
     * @param node the node.
     * @param graph the graph that contains it.
     * @return the node box.
     */
    public static Box nodeBox(Node node, Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        return nodeBox(node, graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition), graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize));
    }

    /**
     * Returns the box of a node.
     *
     * @param node the node.
     * @param positions the position attribute to use.
     * @param sizes the size attribute to use.
     * @return the node box.
     */
    public static Box nodeBox(Node node, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> sizes) {
        Coordinates position = positions.get(node);
        Coordinates size = sizes != null ? sizes.get(node) : new Coordinates(0, 0);
        return Box.boundingBox(Arrays.asList(position), size.x() / 2, size.y() / 2);
    }

    /**
     * Returns the box of an edge. Uses the default attributes for node
     * position, edge size and control points.
     *
     * @param edge the edge.
     * @param graph the graph that contains it.
     * @return the edge box.
     */
    public static Box edgeBox(Edge edge, Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<Double> edgeWidth = graph.hasEdgeAttribute(StdAttribute.edgeWidth) ? graph.<Double>edgeAttribute(StdAttribute.edgeWidth) : null;
        return edgeBox(edge, positions, edgePoints, edgeWidth);
    }

    /**
     * Returns the box of an edge. The box does not consider the actual source
     * and target node dimensions.
     *
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgePoints the edge control points.
     * @param edgeWidth the edge sizes.
     * @return the edge box.
     */
    public static Box edgeBox(Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<Double> edgeWidth) {
        List<Coordinates> points = edgePoints(edge, positions, edgePoints);
        double maxEdgeWidth = edgeWidth != null ? edgeWidth.get(edge) : 0;
        return Box.boundingBox(points, maxEdgeWidth);
    }

    /**
     * Returns the box of a graph. Uses the default attributes for position,
     * size and control points.
     *
     * @param graph the graph.
     * @return the graph box.
     */
    public static Box graphBox(Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> nodeSizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<Double> edgeWidths = graph.hasEdgeAttribute(StdAttribute.edgeWidth) ? graph.<Double>edgeAttribute(StdAttribute.edgeWidth) : null;
        return graphBox(graph, positions, nodeSizes, edgePoints, edgeWidths);
    }

    /**
     * Returns the box of a graph.
     *
     * @param graph the graph.
     * @param positions the position attribute to use.
     * @param nodeSizes the node size attribute to use. If null, all nodes have
     * size 0.
     * @param edgePoints the edge control points attribute to use. If null, all
     * edges have none.
     * @param edgeWidths the edge width attribute to use. It null, all edges have
     * size 0.
     * @return the graph box.
     */
    public static Box graphBox(Graph graph, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> nodeSizes, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<Double> edgeWidths) {
        List<Box> boxes = new ArrayList<>();
        for (Node node : graph.nodes()) {
            boxes.add(nodeBox(node, positions, nodeSizes));
        }
        for (Edge edge : graph.edges()) {
            boxes.add(edgeBox(edge, positions, edgePoints, edgeWidths));
        }
        return Box.combine(boxes);
    }

    /**
     * Computes the radius of a node glyph for a given angle.
     *
     * @param graph the graph.
     * @param node the node.
     * @param angleInRadians the angle to be used to compute the radius.
     * @return the distance between glyph center and glyph boundary at the given
     * angle.
     */
    public static double nodeGlyphRadiusAtAngle(Graph graph, Node node, double angleInRadians) {
        NodeAttribute<Coordinates> sizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        NodeAttribute<NodeShape> shapes = graph.hasNodeAttribute(StdAttribute.nodeShape) ? graph.<NodeShape>nodeAttribute(StdAttribute.nodeShape) : null;
        return nodeGlyphRadiusAtAngle(graph, node, angleInRadians, sizes, shapes);
    }

    /**
     * Computes the radius of a node glyph for a given angle.
     *
     * @param graph the graph.
     * @param node the node.
     * @param angleInRadians the angle to be used to compute the radius.
     * @param sizes the node sizes.
     * @param shapes the node shapes.
     * @return the distance between glyph center and glyph boundary at the given
     * angle.
     */
    public static double nodeGlyphRadiusAtAngle(Graph graph, Node node, double angleInRadians, NodeAttribute<Coordinates> sizes, NodeAttribute<NodeShape> shapes) {
        if (sizes == null) {
            return 0;
        }
        Coordinates size = sizes.get(node);
        NodeShape shape = shapes == null ? NodeShape.cuboid : shapes.get(node);
        switch (shape) {
            case cuboid:
                double normalisedAngle = GeomXD.posNormalizeRadiansAngle(angleInRadians);
                if (normalisedAngle > Math.PI) {
                    normalisedAngle = normalisedAngle - Math.PI;
                }
                if (normalisedAngle > Math.PI / 2.0) {
                    normalisedAngle = Math.PI - normalisedAngle;
                }
                if (normalisedAngle < Math.atan(size.y() / size.x())) {
                    double x = size.x() / 2;
                    double y = size.x() / 2 * Math.tan(normalisedAngle);
                    return Math.sqrt(x * x + y * y);
                } else {
                    double x = size.y() / 2;
                    double y = size.y() / (2 * Math.tan(normalisedAngle));
                    return Math.sqrt(x * x + y * y);
                }
            case spheroid:
                double x = Math.cos(angleInRadians) * size.x() / 2;
                double y = Math.sin(angleInRadians) * size.y() / 2;
                return Math.sqrt(x * x + y * y);
            default:
                throw new UnsupportedOperationException("The shape " + shape + " is not supported yet.");
        }
    }

    /**
     * Computes the approximate distance between a point and the glyph of a
     * node. It is not meant to be precise, especially for irregular node
     * shapes.
     *
     * @param graph the graph.
     * @param point the point.
     * @param node the node.
     * @return the distance between the point and the glyph of the node.
     */
    public static double pointNodeGlyphDistance(Graph graph, Coordinates point, Node node) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        NodeAttribute<NodeShape> shapes = graph.hasNodeAttribute(StdAttribute.nodeShape) ? graph.<NodeShape>nodeAttribute(StdAttribute.nodeShape) : null;
        return pointNodeGlyphDistance(graph, point, node, positions, sizes, shapes);
    }

    /**
     * Computes the approximate distance between a point and the glyph of a
     * node. It is not meant to be precise, especially for irregular node
     * shapes.
     *
     * @param graph the graph.
     * @param point the point.
     * @param node the node.
     * @param positions the node positions.
     * @param sizes the node sizes.
     * @param shapes the node shapes.
     * @return the distance between the point and the glyph of the node.
     */
    public static double pointNodeGlyphDistance(Graph graph, Coordinates point, Node node, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> sizes, NodeAttribute<NodeShape> shapes) {
        Coordinates posNode = positions.get(node);
        double angle = 0;
        Coordinates pointNode = point.minus(posNode);
        if (!Geom2D.almostEqual(point, posNode)) {
            angle = Geom2D.angle(pointNode);
        }
        double distance = Geom2D.magnitude(pointNode);
        distance -= nodeGlyphRadiusAtAngle(graph, node, angle, sizes, shapes);
        return distance;
    }

    /**
     * Computes the approximate distance between the glyph of two nodes. It is
     * not meant to be precise, especially for irregular node shapes.
     *
     * @param graph the graph.
     * @param a the first node.
     * @param b the second node.
     * @return the distance between the glyph of the nodes.
     */
    public static double nodeNodeGlyphDistance(Graph graph, Node a, Node b) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        NodeAttribute<NodeShape> shapes = graph.hasNodeAttribute(StdAttribute.nodeShape) ? graph.<NodeShape>nodeAttribute(StdAttribute.nodeShape) : null;
        return nodeNodeGlyphDistance(graph, a, b, positions, sizes, shapes);
    }

    /**
     * Computes the approximate distance between the glyph of two nodes. It is
     * not meant to be precise, especially for irregular node shapes.
     *
     * @param graph the graph.
     * @param a the first node.
     * @param b the second node.
     * @param positions the node positions.
     * @param sizes the node sizes.
     * @param shapes the node shapes.
     * @return the distance between the glyph of the nodes.
     */
    public static double nodeNodeGlyphDistance(Graph graph, Node a, Node b, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> sizes, NodeAttribute<NodeShape> shapes) {
        Coordinates posA = positions.get(a);
        Coordinates posB = positions.get(b);
        double angleA = 0;
        double angleB = 0;
        Coordinates ab = posB.minus(posA);
        if (!Geom2D.almostEqual(posA, posB)) {
            angleA = Geom2D.angle(ab);
            angleB = angleA + Math.PI;
        }
        double distance = Geom2D.magnitude(ab);
        distance -= nodeGlyphRadiusAtAngle(graph, a, angleA, sizes, shapes);
        distance -= nodeGlyphRadiusAtAngle(graph, b, angleB, sizes, shapes);
        return distance;
    }

    /**
     * Computes the approximate distance between a point and the glyph of an
     * edge. Currently, the edge width is computed as the maximum between source
     * and target width.
     *
     * @param graph the graph.
     * @param point the point.
     * @param edge the edge.
     * @return the distance between the point and the edge glyph.
     */
    public static double pointEdgeGlyphDistance(Graph graph, Coordinates point, Edge edge) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<Double> edgeWidths = graph.hasEdgeAttribute(StdAttribute.edgeWidth) ? graph.<Double>edgeAttribute(StdAttribute.edgeWidth) : null;
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<EdgeShape> edgeShapes = graph.hasEdgeAttribute(StdAttribute.edgeShape) ? graph.<EdgeShape>edgeAttribute(StdAttribute.edgeShape) : null;
        return pointEdgeGlyphDistance(graph, point, edge, positions, edgeWidths, edgePoints, edgeShapes);
    }

    /**
     * Computes the approximate distance between a point and the glyph of an
     * edge. Currently, the edge width is computed as the maximum between source
     * and target width.
     *
     * @param graph the graph.
     * @param point the point.
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgeWidths the edge widths.
     * @param edgePoints the edge control points.
     * @param edgeShapes the edge shapes.
     * @return the distance between the point and the edge glyph.
     */
    public static double pointEdgeGlyphDistance(Graph graph, Coordinates point, Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<Double> edgeWidths, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<EdgeShape> edgeShapes) {
        Coordinates closestEdgePoint = closestEdgePoint(graph, point, edge, positions, edgePoints, edgeShapes);
        double edgeWidth = edgeWidths != null ? edgeWidths.get(edge) : 0;
        return Geom2D.magnitude(point.minus(closestEdgePoint)) - edgeWidth / 2;
    }

    /**
     * Compute the approximate distance between the glyph of a node and that of
     * an edge. It is not meant to be precise, especially for irregular node
     * shapes. Currently, the edge width is computed as the maximum between
     * source and target width.
     *
     * @param graph the graph.
     * @param node the node.
     * @param edge the edge.
     * @return the distance between a node and an edge glyph.
     */
    public static double nodeEdgeGlyphDistance(Graph graph, Node node, Edge edge) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        NodeAttribute<NodeShape> shapes = graph.hasNodeAttribute(StdAttribute.nodeShape) ? graph.<NodeShape>nodeAttribute(StdAttribute.nodeShape) : null;
        EdgeAttribute<Double> edgeWidths = graph.hasEdgeAttribute(StdAttribute.edgeWidth) ? graph.<Double>edgeAttribute(StdAttribute.edgeWidth) : null;
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<EdgeShape> edgeShapes = graph.hasEdgeAttribute(StdAttribute.edgeShape) ? graph.<EdgeShape>edgeAttribute(StdAttribute.edgeShape) : null;
        return nodeEdgeGlyphDistance(graph, node, edge, positions, sizes, shapes, edgeWidths, edgePoints, edgeShapes);
    }

    /**
     * Compute the approximate distance between the glyph of a node and that of
     * an edge. It is not meant to be precise, especially for irregular node
     * shapes. Currently, the edge width is computed as the maximum between
     * source and target width.
     *
     * @param graph the graph.
     * @param node the node.
     * @param edge the edge.
     * @param positions the node positions.
     * @param sizes the node sizes.
     * @param shapes the node shapes.
     * @param edgeWidths the edge widths.
     * @param edgePoints the edge control points.
     * @param edgeShapes the edge shapes.
     * @return the distance between a node and an edge glyph.
     */
    public static double nodeEdgeGlyphDistance(Graph graph, Node node, Edge edge, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> sizes, NodeAttribute<NodeShape> shapes, EdgeAttribute<Double> edgeWidths, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<EdgeShape> edgeShapes) {
        Coordinates nodePos = positions.get(node);
        Coordinates nodeSize = sizes != null ? sizes.get(node) : new Coordinates(0, 0);
        NodeShape nodeShape = shapes != null ? shapes.get(node) : NodeShape.cuboid;
        EdgeShape edgeShape = edgeShapes != null ? edgeShapes.get(edge) : EdgeShape.polyline;
        Double edgeWidth = edgeWidths != null ? edgeWidths.get(edge) : 0.0;
        switch (edgeShape) {
            case polyline:
                List<Coordinates> corners = new ArrayList<>();
                double halfWidth = nodeSize.x() / 2;
                double halfHeight = nodeSize.y() / 2;
                switch (nodeShape) {
                    case cuboid:
                        corners.add(new Coordinates(nodePos.x() + halfWidth, nodePos.y() + halfHeight));
                        corners.add(new Coordinates(nodePos.x() - halfWidth, nodePos.y() + halfHeight));
                        corners.add(new Coordinates(nodePos.x() - halfWidth, nodePos.y() - halfHeight));
                        corners.add(new Coordinates(nodePos.x() + halfWidth, nodePos.y() - halfHeight));
                        break;
                    case spheroid:
                        corners.add(new Coordinates(nodePos.x() + halfWidth, nodePos.y()));
                        corners.add(new Coordinates(nodePos.x() - halfWidth, nodePos.y()));
                        corners.add(new Coordinates(nodePos.x(), nodePos.y() - halfHeight));
                        corners.add(new Coordinates(nodePos.x(), nodePos.y() + halfHeight));
                        break;
                    default:
                        throw new UnsupportedOperationException("The node shape " + edgeShape + " is not supported yet.");
                }
                double distance = Double.POSITIVE_INFINITY;
                for (Coordinates corner : corners) {
                    Coordinates closestPoint = closestEdgePoint(graph, corner, edge, positions, edgePoints, edgeShapes);
                    Coordinates pointToPoint = closestPoint.minus(nodePos);
                    double candidateDistance = Geom2D.magnitude(pointToPoint);
                    candidateDistance -= nodeGlyphRadiusAtAngle(graph, node, Geom2D.angle(pointToPoint), sizes, shapes);
                    candidateDistance -= edgeWidth / 2;
                    distance = Math.min(distance, candidateDistance);
                }
                return distance;
            default:
                throw new UnsupportedOperationException("The edge shape " + edgeShape + " is not supported yet.");
        }
    }

    /**
     * Checks if the nodes in the graph overlap with each other.
     *
     * @param graph the graph.
     * @return true if they overlap, false otherwise.
     */
    public static boolean doNodesOverlap(Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> nodeSizes = graph.hasNodeAttribute(StdAttribute.nodeSize) ? graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize) : null;
        return doNodesOverlap(graph, positions, nodeSizes);
    }

    /**
     * Checks if the nodes in the graph overlap with each other.
     *
     * @param graph the graph.
     * @param positions the node positions.
     * @param nodeSizes the node sizes.
     * @return true if they overlap, false otherwise.
     */
    public static boolean doNodesOverlap(Graph graph, NodeAttribute<Coordinates> positions, NodeAttribute<Coordinates> nodeSizes) {
        for (Node node : graph.nodes()) {
            Box nodeBox = nodeBox(node, positions, nodeSizes);
            for (Node otherNode : graph.nodes()) {
                if (node.compareTo(otherNode) < 0) {
                    Box otherNodeBox = nodeBox(otherNode, positions, nodeSizes);
                    if (nodeBox.intersect(otherNodeBox) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
