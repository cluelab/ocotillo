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
package ocotillo.gui;

import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Attribute;
import ocotillo.graph.Element;
import ocotillo.graph.ElementAttribute;
import ocotillo.graph.ElementAttributeObserver;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphAttribute;
import ocotillo.graph.GraphAttributeObserver;
import ocotillo.graph.GraphObserver;
import ocotillo.graph.Observer;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.GraphRenderer;
import static ocotillo.graph.rendering.GraphRenderer.scaling;
import ocotillo.graph.rendering.Rendering2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panned-zoomed canvas for depicting graphs.
 */
public class GraphCanvas extends JPanel {

    private final Graph graph;
    private final GraphRenderer rendered;

    private final ZoomAndPanListener zoomAndPanListener;
    private final List<Observer> observers = new ArrayList<>();

    private boolean firstPaint = true;
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a graph canvas.
     *
     * @param graph the graph to visualize.
     */
    public GraphCanvas(Graph graph) {
        this.graph = graph;
        this.rendered = new GraphRenderer(graph);

        this.zoomAndPanListener = new ZoomAndPanListener(this);
        addMouseListener(zoomAndPanListener);
        addMouseMotionListener(zoomAndPanListener);
        addMouseWheelListener(zoomAndPanListener);
        addGraphModificationObservers();
    }

    /**
     * Adds an observer for each graph modification that alter the graph
     * rendering.
     */
    private void addGraphModificationObservers() {
        addGraphAttributeObserver(graph);
        for (GraphAttribute<?> attribute : StdAttribute.thatAffectRendering.graphAttributes(graph)) {
            addGraphAttributeObserver(attribute);
        }
        for (ElementAttribute<?, ?> attribute : StdAttribute.thatAffectRendering.nodeAttributes(graph)) {
            addElementAttributeObserver(attribute);
        }
        for (ElementAttribute<?, ?> attribute : StdAttribute.thatAffectRendering.edgeAttributes(graph)) {
            addElementAttributeObserver(attribute);
        }
    }

    /**
     * Adds a graph observer that force the canvas redrawing on graph
     * modifications.
     *
     * @param graph the graph to observe.
     */
    private void addGraphAttributeObserver(Graph graph) {
        observers.add(new GraphObserver(graph) {

            @Override
            public void updateElements(Collection<Element> changedElements) {
                repaint();
            }

            @Override
            public void updateSubGraphs(Collection<Graph> changedSubGraphs) {
                repaint();
            }

            @Override
            public void updateAttributes(Collection<Attribute<?>> changedAttributes) {
            }
        });
    }

    /**
     * Adds an attribute observer that force the canvas redrawing on
     * modifications of the attribute.
     *
     * @param attribute the attribute to observe.
     */
    private void addGraphAttributeObserver(GraphAttribute<?> attribute) {
        observers.add(new GraphAttributeObserver(attribute) {

            @Override
            public void update() {
                repaint();
            }
        });
    }

    /**
     * Adds an attribute observer that force the canvas redrawing on
     * modifications of the attribute.
     *
     * @param attribute the attribute to observe.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addElementAttributeObserver(ElementAttribute<?, ?> attribute) {
        observers.add(new ElementAttributeObserver(attribute) {

            @Override
            public void update(Collection changedElements) {
                repaint();
            }

            @Override
            public void updateAll() {
                repaint();
            }
        });
    }

    /**
     * Close the graph canvas preventing memory leaks.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void close() {
        removeMouseListener(zoomAndPanListener);
        removeMouseMotionListener(zoomAndPanListener);
        removeMouseWheelListener(zoomAndPanListener);
        for (Observer observer : observers) {
            observer.unregister();
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (firstPaint) {
            Box graphBox = Rendering2D.graphBox(graph);
            zoomAndPanListener.resetView(graphBox.scale(new Coordinates(scaling, scaling)));
            firstPaint = false;
        }
        graphics2D.setTransform(zoomAndPanListener.getTransform());
        rendered.draw(graphics2D);
    }
}
