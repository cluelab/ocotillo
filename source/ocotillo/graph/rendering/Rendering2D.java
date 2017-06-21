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
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.Layout2D;
import ocotillo.graph.rendering.svg.SvgElement;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Collects several methods relevant to the 2D rendering of a graph.
 */
public class Rendering2D {

    /**
     * Computes the size of the given text.
     *
     * @param text the text.
     * @param fontScaling the font scaling.
     * @return the text size.
     */
    public static Coordinates textSize(String text, double fontScaling) {
        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Font referenceFont = new Font("SansSerif", Font.PLAIN, 10);
        FontMetrics referenceFontMetrics = dummyImage.getGraphics().getFontMetrics(referenceFont);
        double referenceWidth = referenceFontMetrics.stringWidth("x");
        double referenceHeight = referenceFontMetrics.getHeight();

        Font currentFont = new Font("SansSerif", Font.PLAIN, (int) (fontScaling * 10));
        FontMetrics currentFontMetrics = dummyImage.getGraphics().getFontMetrics(currentFont);
        double currentWidht = currentFontMetrics.stringWidth(text);
        double currentHeight = currentFontMetrics.getHeight();

        return new Coordinates(currentWidht / referenceWidth, currentHeight / referenceHeight);
    }

    /**
     * Computes the box of the given text.
     *
     * @param text the text.
     * @param center the central position of the text.
     * @param fontScaling the font scaling.
     * @return the text box.
     */
    public static Box textBox(String text, Coordinates center, double fontScaling) {
        Coordinates textSize = textSize(text, fontScaling);
        return new Box(center, textSize);
    }

    /**
     * Returns the rendering box for the given graph.
     *
     * @param graph the graph.
     * @return the rendering box of the graph.
     */
    public static Box graphBox(Graph graph) {
        Box renderingBox = Layout2D.graphBox(graph);

        Box labelBox = labelBox(graph);
        if (labelBox != null) {
            renderingBox = renderingBox.combine(labelBox);
        }

        Box graphicsBox = graphicsBox(graph, null);
        if (graphicsBox != null) {
            renderingBox = renderingBox.combine(graphicsBox);
        }

        return renderingBox;
    }

    /**
     * Computes the box including the nodes labels.
     *
     * @param graph the graph.
     * @return the node label box.
     */
    private static Box labelBox(Graph graph) {
        Box labelBox = null;
        NodeAttribute<Coordinates> nodePositions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<String> nodeLabels = graph.<String>nodeAttribute(StdAttribute.label);
        NodeAttribute<Double> nodeLabelFontScaling = graph.<Double>nodeAttribute(StdAttribute.labelScaling);
        NodeAttribute<Coordinates> nodeLabelOffset = graph.<Coordinates>nodeAttribute(StdAttribute.labelOffset);
        for (Node node : graph.nodes()) {
            String label = nodeLabels.get(node);
            Coordinates center = nodePositions.get(node).plus(nodeLabelOffset.get(node));
            double fontScaling = nodeLabelFontScaling.get(node);
            if (labelBox == null) {
                labelBox = textBox(label, center, fontScaling);
            } else {
                labelBox = labelBox.combine(textBox(label, center, fontScaling));
            }
        }
        return labelBox;
    }

    /**
     * Combine a given box with the graphic box of a given graph.
     *
     * @param graph the graph.
     * @param currentBox the current box.
     * @return the combined box.
     */
    private static Box graphicsBox(Graph graph, Box currentBox) {
        String graphics = GraphicsTools.getLocalGraphics(graph).get();
        List<SvgElement> elements = SvgElement.parseSvg(graphics);
        for (SvgElement element : elements) {
            if (currentBox == null) {
                currentBox = element.box();
            } else {
                currentBox = currentBox.combine(element.box());
            }
        }

        for (Graph subgraph : graph.subGraphs()) {
            currentBox = graphicsBox(subgraph, currentBox);
        }
        return currentBox;
    }

    /**
     * Resizes a node to make it sufficient to contain its label. Uses the
     * default margin of 0.25.
     *
     * @param node the node.
     * @param graph the graph.
     */
    public static void resizeGlyphToFitLabel(Node node, Graph graph) {
        resizeGlyphToFitLabel(node, graph, 0.25);
    }

    /**
     * Resizes a node to make it sufficient to contain its label.
     *
     * @param node the node.
     * @param graph the graph.
     * @param margin the margin to be used.
     */
    public static void resizeGlyphToFitLabel(Node node, Graph graph, double margin) {
        NodeAttribute<Coordinates> nodeSizes = graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<String> nodeLabels = graph.<String>nodeAttribute(StdAttribute.label);
        NodeAttribute<Double> nodeLabelFontScaling = graph.<Double>nodeAttribute(StdAttribute.labelScaling);
        resizeGlyphToFitLabel(node, nodeSizes, nodeLabels, nodeLabelFontScaling, margin);
    }

    /**
     * Resizes a node to make it sufficient to contain its label. Uses the
     * default margin of 0.25.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     */
    public static void resizeGlyphToFitLabel(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling) {
        resizeGlyphToFitLabel(node, nodeSizes, nodeLabels, nodeLabelFontScaling, 0.25);
    }

    /**
     * Resizes a node to make it sufficient to contain its label.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     * @param margin the margin to be used.
     */
    public static void resizeGlyphToFitLabel(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling, double margin) {
        String label = nodeLabels.get(node);
        double fontScaling = nodeLabelFontScaling.get(node);
        Coordinates textSize = textSize(label, fontScaling);
        nodeSizes.set(node, textSize.plusIP(new Coordinates(2 * margin, 2 * margin)));
    }

}
