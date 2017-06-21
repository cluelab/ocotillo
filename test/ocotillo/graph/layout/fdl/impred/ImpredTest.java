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
import ocotillo.geometry.Polygon;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.fdl.impred.Impred.ImpredBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ImpredTest {

    @Test
    public void testEdgeAttractionDynamic() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(8, 5));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.EdgeAttraction(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .build();

        impred.iterate(20);

        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(lessThan(1.0)));
    }

    @Test
    public void testNodeNodeRepulsionDynamic() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 2));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeNodeRepulsion(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .build();

        impred.iterate(20);

        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(greaterThan(5.0)));
    }

    @Test
    public void testNodeNodeRepulsionVersusEdgeAttractionDynamic() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));

        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(0, 0));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.EdgeAttraction(5))
                .withForce(new ImpredForce.NodeNodeRepulsion(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .build();

        impred.iterate(20);

        assertThat(positions.get(a), isAlmost(new Coordinates(0, 0)));
        assertThat(positions.get(b), isAlmost(new Coordinates(0, 5)));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 2));
        impred.iterate(20);

        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(greaterThan(4.5)));
        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(lessThan(5.5)));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 7));
        impred.iterate(20);

        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(greaterThan(4.5)));
        assertThat(Geom2D.magnitude(positions.get(b).minus(positions.get(a))), is(lessThan(5.5)));
        
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 7));
        sizes.set(a, new Coordinates(2, 2));
        sizes.set(b, new Coordinates(2, 2));
        impred.iterate(20);

        assertThat(positions.get(a), isAlmost(new Coordinates(0, 0)));
        assertThat(positions.get(b), isAlmost(new Coordinates(0, 7)));

    }

    @Test
    public void testEdgeNodeRepulsionDynamic() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(c, new Coordinates(1, 3));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.EdgeNodeRepulsion(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .build();

        impred.iterate(20);

        assertThat(Geom2D.pointToSegmentDistance(positions.get(c), positions.get(a), positions.get(b)), is(greaterThan(5.0)));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(c, new Coordinates(-1, 2));

        impred.iterate(20);
        
        assertThat(Geom2D.pointToSegmentDistance(positions.get(c), positions.get(a), positions.get(b)), is(greaterThan(5.0)));
    }

    @Test
    public void testNodeAttractionToPointDynamic() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(20, 0));
        positions.set(b, new Coordinates(7, 1));
        positions.set(c, new Coordinates(3, -6));

        NodeAttribute<Coordinates> attrPoint = graph.newNodeAttribute("attraction", new Coordinates(0, 0));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeAttractionToPoint(attrPoint, true))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .build();

        impred.iterate(20);

        assertThat(Geom2D.magnitude(positions.get(a).minus(new Coordinates(0, 0))), is(lessThan(1.0)));
        assertThat(Geom2D.magnitude(positions.get(b).minus(new Coordinates(0, 0))), is(lessThan(1.0)));
        assertThat(Geom2D.magnitude(positions.get(c).minus(new Coordinates(0, 0))), is(lessThan(1.0)));
    }

    @Test
    public void testSurroundingEdgeDynamic() {
        Graph graph = new Graph();
        Node boxNode = graph.newNode();
        Edge boxEdge = graph.newEdge(boxNode, boxNode);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> bends = graph.edgeAttribute(StdAttribute.edgePoints);
        bends.set(boxEdge, new ControlPoints(new Coordinates(2, 0), new Coordinates(2, 2), new Coordinates(0, 2)));

        Collection<Node> elements = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Node element = graph.newNode();
            positions.set(element, new Coordinates(GeomXD.randomDouble(0.1, 1.9), GeomXD.randomDouble(0.1, 1.9)));
            elements.add(element);
        }

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<Collection<Edge>>(Arrays.asList(boxEdge));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.EdgeAttraction(5))
                .withForce(new ImpredForce.NodeNodeRepulsion(5))
                .withForce(new ImpredForce.EdgeNodeRepulsion(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .withConstraint(new ImpredConstraint.SurroundingEdges(surroundingEdges))
                .build();

        impred.iterate(50);

        Polygon box = new Polygon();
        box.add(positions.get(boxNode));
        box.addAll(bends.get(boxEdge));

        for (Node element : elements) {
            assertThat(Geom2D.isPointInPolygon(positions.get(element), box), is(true));
        }
    }

    @Test
    public void testSurroundingEdgeDynamicWithRandomForce() {
        Graph graph = new Graph();
        Node boxNode = graph.newNode();
        Edge boxEdge = graph.newEdge(boxNode, boxNode);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> bends = graph.edgeAttribute(StdAttribute.edgePoints);
        bends.set(boxEdge, new ControlPoints(new Coordinates(2, 0), new Coordinates(2, 2), new Coordinates(0, 2)));

        Collection<Node> elements = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Node element = graph.newNode();
            positions.set(element, new Coordinates(GeomXD.randomDouble(0.1, 1.9), GeomXD.randomDouble(0.1, 1.9)));
            elements.add(element);
        }

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<Collection<Edge>>(Arrays.asList(boxEdge));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.RandomForce(5))
                .withConstraint(new ImpredConstraint.DecreasingMaxMovement(3))
                .withConstraint(new ImpredConstraint.SurroundingEdges(surroundingEdges))
                .build();

        impred.iterate(50);

        Polygon box = new Polygon();
        box.add(positions.get(boxNode));
        box.addAll(bends.get(boxEdge));

        for (Node element : elements) {
            assertThat(Geom2D.isPointInPolygon(positions.get(element), box), is(true));
        }
    }

    @Test
    public void testExpandFlexibleEdges() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 100));

        EdgeAttribute<ControlPoints> edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);

        Impred impred = new ImpredBuilder(graph)
                .withPostProcessing(new ImpredPostProcessing.FlexibleEdges(Arrays.asList(ab), 0, 30))
                .build();

        impred.iterate(1);

        assertThat(graph.edgeCount(), is(1));
        assertThat(edgePoints.get(ab).size(), is(1));
        assertThat(edgePoints.get(ab).get(0), is(new Coordinates(0, 50)));

        impred.iterate(1);

        assertThat(graph.edgeCount(), is(1));
        assertThat(edgePoints.get(ab).size(), is(3));
        assertThat(edgePoints.get(ab).get(0), is(new Coordinates(0, 25)));
        assertThat(edgePoints.get(ab).get(1), is(new Coordinates(0, 50)));
        assertThat(edgePoints.get(ab).get(2), is(new Coordinates(0, 75)));

        impred.iterate(1);

        assertThat(graph.edgeCount(), is(1));
        assertThat(edgePoints.get(ab).size(), is(3));
        assertThat(edgePoints.get(ab).get(0), is(new Coordinates(0, 25)));
        assertThat(edgePoints.get(ab).get(1), is(new Coordinates(0, 50)));
        assertThat(edgePoints.get(ab).get(2), is(new Coordinates(0, 75)));
    }

    @Test
    public void testContractFlexibleEdges() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 100));

        EdgeAttribute<ControlPoints> edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        ControlPoints abPoints = new ControlPoints();
        abPoints.add(new Coordinates(0, 19));
        abPoints.add(new Coordinates(5, 20));
        abPoints.add(new Coordinates(0, 21));
        abPoints.add(new Coordinates(0, 49));
        abPoints.add(new Coordinates(-5, 50));
        abPoints.add(new Coordinates(0, 51));
        edgePoints.set(ab, abPoints);

        Node nodeOnTheWay = graph.newNode();
        positions.set(nodeOnTheWay, new Coordinates(-1, 50));

        Impred impred = new ImpredBuilder(graph)
                .withPostProcessing(new ImpredPostProcessing.FlexibleEdges(Arrays.asList(ab), 5, 100))
                .build();

        assertThat(graph.edgeCount(), is(1));
        assertThat(edgePoints.get(ab).size(), is(6));

        impred.iterate(1);

        assertThat(graph.edgeCount(), is(1));
        assertThat(edgePoints.get(ab).size(), is(5));
        assertThat(edgePoints.get(ab).get(0), is(new Coordinates(0, 19)));
        assertThat(edgePoints.get(ab).get(1), is(new Coordinates(0, 21)));
        assertThat(edgePoints.get(ab).get(2), is(new Coordinates(0, 49)));
        assertThat(edgePoints.get(ab).get(3), is(new Coordinates(-5, 50)));
        assertThat(edgePoints.get(ab).get(4), is(new Coordinates(0, 51)));
    }

}
