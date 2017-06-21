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

import ocotillo.graph.Graph;
import ocotillo.graph.GraphAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.svg.SvgElement;
import java.awt.Color;

/**
 * Collects a few methods to facilitate the usage of the graphics attribute.
 */
public class GraphicsTools {

    /**
     * Converts a color in its hexadecimal representation.
     *
     * @param color the color.
     * @return its hexadecimal representation.
     */
    public static String colorHexWriter(Color color) {
        return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Converts a hexadecimal representation of the color in a color object.
     *
     * @param colorHex the color code.
     * @return the color.
     */
    public static Color colorHexReader(String colorHex) {
        Color color = Color.decode(colorHex.substring(0, Math.min(7, colorHex.length())));
        if (colorHex.length() <= 7) {
            return color;
        } else {
            int alpha = Integer.parseInt(colorHex.substring(7), 16);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }
    }

    /**
     * Adds a graphic definition in front of the graphics for the graph.
     *
     * @param graph the graph.
     * @param graphicsString the graphics definitions to add.
     */
    public static void addGraphicsFirst(Graph graph, String graphicsString) {
        GraphAttribute<String> graphics = getGraphics(graph);
        if (graphics.get().isEmpty()) {
            graphics.set(graphicsString);
        } else {
            graphics.set(graphicsString + " " + graphics.get());
        }
    }

    /**
     * Adds an SVG element in front of the graphics for the graph.
     *
     * @param graph the graph.
     * @param element the SVG element to add.
     */
    public static void addGraphicsFirst(Graph graph, SvgElement element) {
        GraphicsTools.addGraphicsFirst(graph, element.toString());
    }

    /**
     * Adds a graphic definition at the back of the graphics for the graph.
     *
     * @param graph the graph.
     * @param graphicsString the graphics definitions to add.
     */
    public static void addGraphicsLast(Graph graph, String graphicsString) {
        GraphAttribute<String> graphics = getGraphics(graph);
        if (graphics.get().isEmpty()) {
            graphics.set(graphicsString);
        } else {
            graphics.set(graphics.get() + " " + graphicsString);
        }
    }

    /**
     * Adds an SVG element at the back of the graphics for the graph.
     *
     * @param graph the graph.
     * @param element the SVG element to add.
     */
    public static void addGraphicsLast(Graph graph, SvgElement element) {
        GraphicsTools.addGraphicsLast(graph, element.toString());
    }

    /**
     * Adds a graphic definition in front of the local graphics for the graph.
     *
     * @param graph the graph.
     * @param graphicsString the graphics definitions to add.
     */
    public static void addLocalGraphicFirst(Graph graph, String graphicsString) {
        GraphAttribute<String> graphics = getLocalGraphics(graph);
        if (graphics.get().isEmpty()) {
            graphics.set(graphicsString);
        } else {
            graphics.set(graphicsString + " " + graphics.get());
        }
    }

    /**
     * Adds an SVG element in front of the local graphics for the graph.
     *
     * @param graph the graph.
     * @param element the SVG element to add.
     */
    public static void addLocalGraphicFirst(Graph graph, SvgElement element) {
        GraphicsTools.addLocalGraphicFirst(graph, element.toString());
    }

    /**
     * Adds a graphic definition at the back of the local graphics for the
     * graph.
     *
     * @param graph the graph.
     * @param graphicsString the graphics definitions to add.
     */
    public static void addLocalGraphicLast(Graph graph, String graphicsString) {
        GraphAttribute<String> graphics = getLocalGraphics(graph);
        if (graphics.get().isEmpty()) {
            graphics.set(graphicsString);
        } else {
            graphics.set(graphics.get() + " " + graphicsString);
        }
    }

    /**
     * Adds an SVG element at the back of the local graphics for the graph.
     *
     * @param graph the graph.
     * @param element the SVG element to add.
     */
    public static void addLocalGraphicLast(Graph graph, SvgElement element) {
        GraphicsTools.addLocalGraphicLast(graph, element.toString());
    }

    /**
     * Gets a graphic attribute for the graph.
     *
     * @param graph the graph.
     * @return a graphics attribute in the graph hierarchy.
     */
    public static GraphAttribute<String> getGraphics(Graph graph) {
        return graph.graphAttribute(StdAttribute.graphics);
    }

    /**
     * Gets a local graphic attribute for the graph. If the local attribute does
     * not exists, it creates it.
     *
     * @param graph the graph.
     * @return its local graphic attribute.
     */
    public static GraphAttribute<String> getLocalGraphics(Graph graph) {
        GraphAttribute<String> graphics;
        if (graph.hasLocalGraphAttribute(StdAttribute.graphics)) {
            graphics = graph.graphAttribute(StdAttribute.graphics);
        } else {
            graphics = graph.newLocalGraphAttribute(StdAttribute.graphics, "");
        }
        return graphics;
    }

}
