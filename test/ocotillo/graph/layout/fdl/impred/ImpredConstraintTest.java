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
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.ElementAttributeObserver;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.impred.Impred.ImpredBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ImpredConstraintTest {

    @Test
    public void testEdgeAttraction() {
        Graph graph = new Graph();
        final Node a = graph.newNode();

        Impred impred = new ImpredBuilder(graph)
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(5))
                .build();

        final NodeAttribute<Double> constraints = Whitebox.getInternalState(impred, "constraints");

        new ElementAttributeObserver<Node>(constraints) {

            double previousValue = 5.1;

            @Override
            public void update(Collection<Node> changedElements) {
                Node changedElement = changedElements.iterator().next();
                double currentValue = constraints.get(changedElement);
                assertThat(changedElement, is(a));
                assertThat(currentValue, is(lessThan(previousValue)));
                previousValue = currentValue;
            }

            @Override
            public void updateAll() {
            }
        };

        impred.iterate(10);
    }

    @Test
    public void SurroundingEdgesProjectionInside() {
        Graph graph = new Graph();

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, -2));
        positions.set(b, new Coordinates(2, 2));

        NodeAttribute<Coordinates> moveTo = new NodeAttribute<>(new Coordinates(0, 0));
        moveTo.set(a, new Coordinates(0, 0));
        moveTo.set(b, new Coordinates(0, 2));
        moveTo.set(c, Geom2D.unitVector(0));
        moveTo.set(d, Geom2D.unitVector(Math.PI / 4));
        moveTo.set(e, Geom2D.unitVector(Math.PI / 3));
        moveTo.set(f, Geom2D.unitVector(Math.PI / 2));
        moveTo.set(g, Geom2D.unitVector(Math.PI));

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<Collection<Edge>>(Arrays.asList(ab));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeAttractionToPoint(moveTo, true))
                .withConstraint(new ImpredConstraint.SurroundingEdges(surroundingEdges))
                .build();

        impred.iterate(1);

        NodeAttribute<Double> constraints = Whitebox.getInternalState(impred, "constraints");

        assertThat(constraints.get(a), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(b), isAlmost(1));
        assertThat(constraints.get(c), isAlmost(1));
        assertThat(constraints.get(d), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(e), isAlmost(2));
        assertThat(constraints.get(f), is(Double.POSITIVE_INFINITY));
        assertThat(constraints.get(g), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void SurroundingEdgesProjectionOutside() {
        Graph graph = new Graph();

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2));
        positions.set(b, new Coordinates(2, 4));

        NodeAttribute<Coordinates> moveTo = new NodeAttribute<>(new Coordinates(0, 0));
        moveTo.set(a, new Coordinates(0, 0));
        moveTo.set(b, new Coordinates(0, 2));
        moveTo.set(c, Geom2D.unitVector(0));
        moveTo.set(d, Geom2D.unitVector(Math.PI / 4));
        moveTo.set(e, Geom2D.unitVector(Math.PI / 2));
        moveTo.set(f, Geom2D.unitVector(Math.PI));

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<Collection<Edge>>(Arrays.asList(ab));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeAttractionToPoint(moveTo, true))
                .withConstraint(new ImpredConstraint.SurroundingEdges(surroundingEdges))
                .build();

        impred.iterate(1);

        NodeAttribute<Double> constraints = Whitebox.getInternalState(impred, "constraints");

        assertThat(constraints.get(a), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(b), isAlmost(Math.sqrt(2) * 2));
        assertThat(constraints.get(c), isAlmost(2));
        assertThat(constraints.get(d), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(e), isAlmost(2));
        assertThat(constraints.get(f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testPinnedNodes() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Boolean> pinnedNodesA = new NodeAttribute<>(false);
        pinnedNodesA.set(a, true);

        Collection<Node> pinnedNodesB = new ArrayList<>();
        pinnedNodesB.add(b);

        Impred impred = new ImpredBuilder(graph)
                .withConstraint(new ImpredConstraint.PinnedNodes(pinnedNodesA))
                .withConstraint(new ImpredConstraint.PinnedNodes(pinnedNodesB))
                .build();

        NodeAttribute<Double> constraints = Whitebox.getInternalState(impred, "constraints");

        impred.iterate(1);

        assertThat(constraints.get(a), is(0.0));
        assertThat(constraints.get(b), is(0.0));
        assertThat(constraints.get(c), is(Double.POSITIVE_INFINITY));
    }
}
