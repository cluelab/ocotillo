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
package ocotillo.graph.serialization.oco;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OcoWriterTest {

    @Test
    public void testWriteGraph() {
        Graph graph = new Graph();
        graph.newGraphAttribute(StdAttribute.label, "myGraph");
        graph.newGraphAttribute("myMetric", 2.0);
        
        OcoSaver saver = new OcoSaver();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();
        
        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute\tlabel\tmyMetric"));
        assertThat(iterator.next(), is("@type\tString\tDouble"));
        assertThat(iterator.next(), is("@default\tmyGraph\t2.0"));
    }

    @Test
    public void testWriteNodes() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");

        NodeAttribute<Integer> ranks = graph.newNodeAttribute("myRank", 10);
        ranks.set(mario, 1);
        ranks.set(luigi, 2);
        
        NodeAttribute<Coordinates> sizes = graph.nodeAttribute(StdAttribute.nodeSize);
        sizes.set(mario, new Coordinates(2, 3));
        sizes.set(luigi, new Coordinates(4, 5));
        
        OcoSaver saver = new OcoSaver();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();
        
        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute\tmyRank\tnodeSize"));
        assertThat(iterator.next(), is("@type\tInteger\tCoordinates"));
        assertThat(iterator.next(), is("@default\t10\t1.0,1.0"));
        assertThat(iterator.next(), is("luigi\t2\t4.0,5.0"));
        assertThat(iterator.next(), is("mario\t1\t2.0,3.0"));
    }

    @Test
    public void testWriteEdges() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");
        Node peach = graph.newNode("peach");
        Edge ml = graph.newEdge("ml", mario, luigi);
        Edge lp = graph.newEdge("lp", luigi, peach);
        
        EdgeAttribute<Double> width = graph.edgeAttribute(StdAttribute.edgeWidth);
        width.set(ml, 1.3);
        width.set(lp, 3.4);
        
        EdgeAttribute<Color> myColor = graph.newEdgeAttribute("myColor", Color.RED);
        myColor.set(ml, Color.BLACK);
        myColor.set(lp, Color.GREEN);
        
        OcoSaver saver = new OcoSaver();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();
        
        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is("peach"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\t@from\t@to\tedgeWidth\tmyColor"));
        assertThat(iterator.next(), is("@type\t\t\tDouble\tColor"));
        assertThat(iterator.next(), is("@default\t\t\t0.2\t#ff0000ff"));
        assertThat(iterator.next(), is("lp\tluigi\tpeach\t3.4\t#00ff00ff"));
        assertThat(iterator.next(), is("ml\tmario\tluigi\t1.3\t#000000ff"));
    }
    
    @Test
    public void testWriteHierarchy() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");
        Node peach = graph.newNode("peach");
        Edge ml = graph.newEdge("ml", mario, luigi);
        Edge lp = graph.newEdge("lp", luigi, peach);
        
        EdgeAttribute<Double> width = graph.edgeAttribute(StdAttribute.edgeWidth);
        width.set(ml, 1.3);
        width.set(lp, 3.4);
        
        Graph subgraph = graph.newSubGraph();
        subgraph.add(mario);
        subgraph.add(luigi);
        subgraph.add(ml);
        
        EdgeAttribute<Color> myColor = subgraph.newLocalEdgeAttribute("myColor", Color.RED);
        myColor.set(ml, Color.BLACK);
        
        OcoSaver saver = new OcoSaver();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();
        
        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is("peach"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\t@from\t@to\tedgeWidth"));
        assertThat(iterator.next(), is("@type\t\t\tDouble"));
        assertThat(iterator.next(), is("@default\t\t\t0.2"));
        assertThat(iterator.next(), is("lp\tluigi\tpeach\t3.4"));
        assertThat(iterator.next(), is("ml\tmario\tluigi\t1.3"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("##graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\tmyColor"));
        assertThat(iterator.next(), is("@type\tColor"));
        assertThat(iterator.next(), is("@default\t#ff0000ff"));
        assertThat(iterator.next(), is("ml\t#000000ff"));
    }
    

}
