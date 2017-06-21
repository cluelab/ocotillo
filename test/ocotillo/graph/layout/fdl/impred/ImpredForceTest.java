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
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.fdl.impred.Impred.ImpredBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ImpredForceTest {

    @Test
    public void testEdgeAttraction() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 3));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.EdgeAttraction(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));

        double originalMagnitude = Geom2D.magnitude(forces.get(a));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 2));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));
        assertThat(Geom2D.magnitude(forces.get(a)), is(lessThan(originalMagnitude)));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 7));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));
        assertThat(Geom2D.magnitude(forces.get(a)), is(greaterThan(originalMagnitude)));
    }

    @Test
    public void testNodeNodeRepulsion() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 3));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeNodeRepulsion(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));

        double originalMagnitude = Geom2D.magnitude(forces.get(a));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 2));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));
        assertThat(Geom2D.magnitude(forces.get(a)), is(greaterThan(originalMagnitude)));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 7));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));
        assertThat(Geom2D.magnitude(forces.get(a)), is(lessThan(originalMagnitude)));
    }

    @Test
    public void testNodeNodeRepulsionVersusEdgeAttraction() {
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
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(0.0));
        assertThat(Geom2D.magnitude(forces.get(b)), isAlmost(0.0));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(b, new Coordinates(0, 2));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(b, new Coordinates(0, 7));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(Geom2D.magnitude(forces.get(b))));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 7));
        sizes.set(a, new Coordinates(2, 2));
        sizes.set(b, new Coordinates(2, 2));

        impred.iterate(1);
        assertThat(Geom2D.magnitude(forces.get(a)), isAlmost(0.0));
        assertThat(Geom2D.magnitude(forces.get(b)), isAlmost(0.0));
    }

    @Test
    public void testEdgeNodeRepulsion() {
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
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom2D.unitVector(forces.get(c)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.magnitude(forces.get(a)), is(lessThan(Geom2D.magnitude(forces.get(b)))));

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(c, new Coordinates(-1, 2));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.unitVector(forces.get(c)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom2D.magnitude(forces.get(a)), is(greaterThan(Geom2D.magnitude(forces.get(b)))));
    }

    @Test
    public void testSelectedEdgeNodeRepulsion() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge bc = graph.newEdge(b, c);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(c, new Coordinates(1, 3));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.SelectedEdgeNodeRepulsion(5, Arrays.asList(ab)))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom2D.unitVector(forces.get(c)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.magnitude(forces.get(a)), is(lessThan(Geom2D.magnitude(forces.get(b)))));
    }

    @Test
    public void testCurveSmoothing() {
        Graph graph = new Graph();
        Node a = graph.newNode("a");
        Node b = graph.newNode("b");
        Node c = graph.newNode("c");
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge ac = graph.newEdge(a, c);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(0, 5));
        positions.set(c, new Coordinates(1, 3));

        EdgeAttribute<ControlPoints> edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        edgePoints.set(ab, new ControlPoints(new Coordinates(0, 1), new Coordinates(-1, 5)));
        edgePoints.set(cb, new ControlPoints(new Coordinates(2, 4), new Coordinates(1, 5)));
        edgePoints.set(ac, new ControlPoints(new Coordinates(1, 0), new Coordinates(2, 2)));

        List<Edge> curve = new ArrayList<>();
        curve.add(ab);
        curve.add(cb);
        curve.add(ac);

        List<List<Edge>> curves = new ArrayList<>();
        curves.add(curve);

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.CurveSmoothing(curves))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(forces.get(a), isAlmost(new Coordinates(1.0 / 3.0, 1.0 / 3.0)));
        assertThat(forces.get(b), isAlmost(new Coordinates(0, 0)));
        assertThat(forces.get(c), isAlmost(new Coordinates(2.0 / 3.0, 0)));
    }

    @Test
    public void testNodeAttractionToPoint() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(-1, 0));
        positions.set(b, new Coordinates(0, 1));
        positions.set(c, new Coordinates(1, 0));

        NodeAttribute<Coordinates> attrPoint = graph.newNodeAttribute("attraction", new Coordinates(0, 0));
        attrPoint.set(a, new Coordinates(0, 0));
        attrPoint.set(b, new Coordinates(0, 0));

        Impred impred = new ImpredBuilder(graph)
                .withForce(new ImpredForce.NodeAttractionToPoint(attrPoint, false))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(impred, "forces");

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(forces.get(c), isAlmost(new Coordinates(0, 0)));

        positions.set(a, new Coordinates(1, 0));
        positions.set(b, new Coordinates(2, 0));
        positions.set(c, new Coordinates(3, 0));
        attrPoint.set(c, new Coordinates(0, 0));

        impred.iterate(1);
        assertThat(Geom2D.magnitude(forces.get(a)), is(lessThan(Geom2D.magnitude(forces.get(b)))));
        assertThat(Geom2D.magnitude(forces.get(b)), is(lessThan(Geom2D.magnitude(forces.get(c)))));

        positions.set(a, new Coordinates(1, 0));
        positions.set(b, new Coordinates(2, 0));
        positions.set(c, new Coordinates(3, 0));

        attrPoint.set(a, new Coordinates(1, 1));
        attrPoint.set(b, new Coordinates(2, 1));
        attrPoint.set(c, new Coordinates(3, 1));

        impred.iterate(1);
        assertThat(Geom2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom2D.unitVector(forces.get(c)), isAlmost(new Coordinates(0, 1)));
    }

}
