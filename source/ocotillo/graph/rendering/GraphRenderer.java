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
package ocotillo.graph.rendering;

import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.graph.rendering.svg.SvgElement;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderer for graphs into graphics objects.
 */
public class GraphRenderer {

    private final Graph graph;
    private final NodeAttribute<Coordinates> nodePositions;
    private final NodeAttribute<Coordinates> nodeSizes;
    private final NodeAttribute<StdAttribute.NodeShape> nodeShapes;
    private final NodeAttribute<Color> nodeColors;
    private final NodeAttribute<String> nodeLabels;
    private final NodeAttribute<Color> nodeLabelColors;
    private final NodeAttribute<Double> nodeLabelScaling;
    private final NodeAttribute<Coordinates> nodeLabelOffset;
    private final EdgeAttribute<Double> edgeWidths;
    private final EdgeAttribute<StdAttribute.EdgeShape> edgeShapes;
    private final EdgeAttribute<StdAttribute.ControlPoints> edgePoints;
    private final EdgeAttribute<Color> edgeColors;
    private final HeatMap heatMap;

    /**
     * The scaling factor used in the drawing. The scaling factor has been
     * empirically determined in order to make the letter X with font scaling 1
     * fit nicely in a node of size 1.
     */
    public static final double scaling = 13;

    /**
     * Constructs a rendered for a given graph.
     *
     * @param graph the graph.
     */
    public GraphRenderer(Graph graph) {
        this.graph = graph;

        this.nodePositions = graph.nodeAttribute(StdAttribute.nodePosition);
        this.nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        this.nodeShapes = graph.nodeAttribute(StdAttribute.nodeShape);
        this.nodeColors = graph.nodeAttribute(StdAttribute.color);
        this.nodeLabels = graph.nodeAttribute(StdAttribute.label);
        this.nodeLabelColors = graph.nodeAttribute(StdAttribute.labelColor);
        this.nodeLabelScaling = graph.nodeAttribute(StdAttribute.labelScaling);
        this.nodeLabelOffset = graph.nodeAttribute(StdAttribute.labelOffset);

        this.edgeWidths = graph.edgeAttribute(StdAttribute.edgeWidth);
        this.edgeShapes = graph.edgeAttribute(StdAttribute.edgeShape);
        this.edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        this.edgeColors = graph.edgeAttribute(StdAttribute.color);

        this.heatMap = new HeatMap();
    }

    /**
     * Draws a graph into a graphics object.
     *
     * @param graphics the graphics.
     */
    public void draw(Graphics2D graphics) {
        drawGraphics(graphics, graph);
        drawHeatMap(graphics);
        drawEdges(graphics);
        drawNodes(graphics);
    }

    /**
     * Draws the graph nodes.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawNodes(Graphics2D graphics2D) {
        for (Node node : new ArrayList<>(graph.nodes())) {
            drawNodeGlyph(graphics2D, node);
            drawNodeLabel(graphics2D, node);
        }
    }

    /**
     * Draws the glyph of a node.
     *
     * @param graphics2D the 2D graphics.
     * @param node the node.
     */
    private void drawNodeGlyph(Graphics2D graphics2D, Node node) {
        Color fillColor = nodeColors.get(node);
        Coordinates size = nodeSizes.get(node);
        Coordinates center = nodePositions.get(node);
        NodeShape shape = nodeShapes.get(node);

        switch (shape) {
            case spheroid:
                ElementRenderer.drawEllipse(graphics2D, center, size, fillColor);
                break;
            case cuboid:
                ElementRenderer.drawRectangle(graphics2D, center, size, fillColor);
                break;
            default:
                throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
        }
    }

    /**
     * Draws the label of a node.
     *
     * @param graphics2D the 2D graphics.
     * @param node the node.
     */
    private void drawNodeLabel(Graphics2D graphics2D, Node node) {
        String label = nodeLabels.get(node);
        Color color = nodeLabelColors.get(node);
        double dimension = nodeLabelScaling.get(node);
        Coordinates position = nodePositions.get(node);
        Coordinates labelOffset = nodeLabelOffset.get(node);
        Coordinates labelPosition = position.plus(labelOffset);
        ElementRenderer.drawText(graphics2D, label, labelPosition, dimension, color);
    }

    /**
     * Draws the graph edges.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawEdges(Graphics2D graphics2D) {
        for (Edge edge : new ArrayList<>(graph.edges())) {
            Double width = edgeWidths.get(edge);
            EdgeShape shape = edgeShapes.get(edge);
            Color color = edgeColors.get(edge);
            Coordinates startingPoint = nodePositions.get(edge.source());
            Coordinates endingPoint = nodePositions.get(edge.target());
            ControlPoints controlPoints = edgePoints.get(edge);

            switch (shape) {
                case polyline:
                    ElementRenderer.drawPolyline(graphics2D, startingPoint, endingPoint, controlPoints, width, color);
                    break;
                default:
                    throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
            }
        }
    }

    /**
     * Draws the heat map.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawHeatMap(Graphics2D graphics2D) {
        if (!graph.nodeAttribute(StdAttribute.nodeHeat).isSleeping()) {
            Image heatMapImage = heatMap.getImage(graph);
            Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
            Box imageBox = heatMap.getImageBox();
            int x = (int) (imageBox.topLeft().x() * scaling);
            int y = (int) (-imageBox.topLeft().y() * scaling);
            int width = (int) (imageBox.width() * scaling);
            int height = (int) (imageBox.height() * scaling);
            graphicsCopy.drawImage(heatMapImage, x, y, width, height, null);
        }
    }

    /**
     * Draws the graphics of the given graph.
     *
     * @param graphics2D the 2D graphics.
     * @param graph the graph.
     */
    private void drawGraphics(Graphics2D graphics2D, Graph graph) {
        if(graph.hasLocalGraphAttribute(StdAttribute.graphics)){
            String graphicsString = graph.<String>graphAttribute(StdAttribute.graphics).get();
            List<SvgElement> svgElements = SvgElement.parseSvg(graphicsString);
            for(SvgElement svgElement : svgElements){
                svgElement.drawYourself(graphics2D);
            }
        }
        
        for (Graph subGraph : graph.subGraphs()) {
            drawGraphics(graphics2D, subGraph);
        }
    }
    
}
