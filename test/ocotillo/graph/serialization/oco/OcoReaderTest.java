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
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.serialization.oco.OcoReader.Block;
import ocotillo.graph.serialization.oco.OcoReader.MalformedFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class OcoReaderTest {

    @Test
    public void testGetBlocks() throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("#graph");
        lines.add("");
        lines.add("");
        lines.add("#nodes");
        lines.add("@attribute \t myLabel");
        lines.add("");
        lines.add("mario \t redPlumber");
        lines.add("luigi \t ");
        lines.add("#edges");
        lines.add("1e \t mario \t luigi");

        LinkedList<Block> blocks = Whitebox.<LinkedList<Block>>invokeMethod(OcoReader.class, "getBlocks", lines);

        assertThat(blocks.size(), is(3));

        assertThat(blocks.get(0).header, is("#graph"));
        assertThat(blocks.get(0).attributeNames, is(new String[]{"@attribute"}));
        assertThat(blocks.get(0).attributeTypes, is(new String[]{"@type"}));
        assertThat(blocks.get(0).attributeDefaults, is(new String[]{"@default"}));
        assertThat(blocks.get(0).values.length, is(0));

        assertThat(blocks.get(1).header, is("#nodes"));
        assertThat(blocks.get(1).attributeNames, is(new String[]{"@attribute", "myLabel"}));
        assertThat(blocks.get(1).attributeTypes, is(new String[]{"@type"}));
        assertThat(blocks.get(1).attributeDefaults, is(new String[]{"@default"}));
        assertThat(blocks.get(1).values.length, is(2));
        assertThat(blocks.get(1).values[0], is(new String[]{"mario", "redPlumber", ""}));
        assertThat(blocks.get(1).values[1], is(new String[]{"luigi", "", ""}));

        assertThat(blocks.get(2).header, is("#edges"));
        assertThat(blocks.get(2).attributeNames, is(new String[]{"@attribute"}));
        assertThat(blocks.get(2).attributeTypes, is(new String[]{"@type"}));
        assertThat(blocks.get(2).attributeDefaults, is(new String[]{"@default"}));
        assertThat(blocks.get(2).values.length, is(1));
        assertThat(blocks.get(2).values[0], is(new String[]{"1e", "mario", "luigi"}));
    }

    @Test
    public void testParseGraph() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("@attribute \t myLabel \t myRate");
        lines.add("@type \t String \t Double");
        lines.add("@default \t my graph \t 8.5");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);

        assertThat(graph.hasGraphAttribute("myLabel"), is(true));
        assertThat(graph.<String>graphAttribute("myLabel").get(), is("my graph"));
        assertThat(graph.hasGraphAttribute("myRate"), is(true));
        assertThat(graph.<Double>graphAttribute("myRate").get(), is(8.5));
    }

    @Test
    public void testParseGraphWithNodes() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("#nodes");
        lines.add("@attribute \t label \t myRate \t nodeSize");
        lines.add("@types \t  \t Double");
        lines.add("@default \t noLabel \t   ");
        lines.add("mario \t redPlumber \t  \t 3,3 ");
        lines.add("luigi \t  \t 7 ");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);

        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.hasNode("mario"), is(true));
        assertThat(graph.hasNode("luigi"), is(true));

        Node mario = graph.getNode("mario");
        Node luigi = graph.getNode("luigi");

        assertThat(graph.hasNodeAttribute(StdAttribute.label), is(true));
        assertThat(graph.<String>nodeAttribute(StdAttribute.label).getDefault(), is("noLabel"));
        assertThat(graph.<String>nodeAttribute(StdAttribute.label).get(mario), is("redPlumber"));
        assertThat(graph.<String>nodeAttribute(StdAttribute.label).isDefault(luigi), is(true));

        assertThat(graph.hasNodeAttribute("myRate"), is(true));
        assertThat(graph.<Double>nodeAttribute("myRate").getDefault(), is(0.0));
        assertThat(graph.<Double>nodeAttribute("myRate").isDefault(mario), is(true));
        assertThat(graph.<Double>nodeAttribute("myRate").get(luigi), is(7.0));

        assertThat(graph.hasNodeAttribute(StdAttribute.nodeSize), is(true));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize).getDefault(), is(new Coordinates(1, 1)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize).get(mario), is(new Coordinates(3, 3)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize).isDefault(luigi), is(true));
    }

    @Test(expected = MalformedFileException.class)
    public void testConflictWithNodeStdAttributeType() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("#nodes");
        lines.add("@attribute \t label \t myRate \t nodeSize");
        lines.add("@types \t Double \t Double");
        lines.add("@default \t noLabel \t   ");
        lines.add("mario \t redPlumber \t  \t 3,3 ");
        lines.add("luigi \t  \t 7 ");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);
    }

    @Test
    public void testParseGraphWithEdges() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("#nodes");
        lines.add("mario");
        lines.add("luigi");
        lines.add("#edges");
        lines.add("@attribute \t @from \t @to   \t edgeWidth \t myMetric");
        lines.add("@types     \t       \t       \t           \t Double");
        lines.add("@default   \t       \t       \t 5         \t 4.5 ");
        lines.add("1          \t mario \t luigi \t 2");
        lines.add("2          \t mario \t luigi \t           \t 6");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);

        assertThat(graph.edgeCount(), is(2));
        assertThat(graph.hasEdge("1"), is(true));
        assertThat(graph.hasEdge("2"), is(true));
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.hasNode("mario"), is(true));
        assertThat(graph.hasNode("luigi"), is(true));

        Node mario = graph.getNode("mario");
        Node luigi = graph.getNode("luigi");
        Edge first = graph.getEdge("1");
        Edge second = graph.getEdge("2");

        assertThat(first.source(), is(mario));
        assertThat(first.target(), is(luigi));
        assertThat(second.source(), is(mario));
        assertThat(second.target(), is(luigi));

        assertThat(graph.hasEdgeAttribute(StdAttribute.edgeWidth), is(true));
        assertThat(graph.<Double>edgeAttribute(StdAttribute.edgeWidth).getDefault(), is(5.0));
        assertThat(graph.<Double>edgeAttribute(StdAttribute.edgeWidth).get(first), is(2.0));
        assertThat(graph.<Double>edgeAttribute(StdAttribute.edgeWidth).isDefault(second), is(true));

        assertThat(graph.hasEdgeAttribute("myMetric"), is(true));
        assertThat(graph.<Double>edgeAttribute("myMetric").getDefault(), is(4.5));
        assertThat(graph.<Double>edgeAttribute("myMetric").isDefault(first), is(true));
        assertThat(graph.<Double>edgeAttribute("myMetric").get(second), is(6.0));
    }

    @Test(expected = MalformedFileException.class)
    public void testConflictWithEdgeStdAttributeType() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("#nodes");
        lines.add("mario");
        lines.add("luigi");
        lines.add("#edges");
        lines.add("@attribute \t @from \t @to   \t edgeWidth   \t myMetric");
        lines.add("@types     \t       \t       \t Coordinates \t Double");
        lines.add("@default   \t       \t       \t 5           \t 4.5 ");
        lines.add("1          \t mario \t luigi \t 2");
        lines.add("2          \t mario \t luigi \t             \t 6");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);
    }

    @Test
    public void testParseGraphWithHierarchy() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("@attribute \t label ");
        lines.add("mario      \t redPlumber");
        lines.add("luigi      \t greenPlumber");
        lines.add("");
        lines.add("#edges");
        lines.add("@attribute \t @from \t @to   \t edgeWidth \t myMetric");
        lines.add("@types     \t       \t       \t           \t Double");
        lines.add("@default   \t       \t       \t 5         \t 4.5 ");
        lines.add("1          \t mario \t luigi \t 2");
        lines.add("2          \t mario \t luigi \t           \t 6");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("##graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("mario");
        lines.add("luigi");
        lines.add("");
        lines.add("#edges");
        lines.add("@attribute \t edgeWidth");
        lines.add("1          \t 20");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("###graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("@attribute \t label ");
        lines.add("mario      \t mushroomEater");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("##graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("luigi");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);

        assertThat(graph.subGraphs().size(), is(2));

        Graph graph_1_1 = new Graph();
        Graph graph_1_2 = new Graph();
        Collection<Graph> subgraphs_1 = graph.subGraphs();
        for (Graph subgraph : subgraphs_1) {
            if (subgraph.nodeCount() == 2) {
                graph_1_1 = subgraph;
            } else {
                graph_1_2 = subgraph;
            }
        }

        assertThat(graph_1_1.nodeCount(), is(2));
        assertThat(graph_1_1.hasLocalEdgeAttribute(StdAttribute.edgeWidth), is(true));
        assertThat(graph_1_1.<Double>edgeAttribute(StdAttribute.edgeWidth).get(graph.getEdge("1")), is(20.0));
        assertThat(graph_1_2.nodeCount(), is(1));
        assertThat(graph_1_2.hasNode("luigi"), is(true));

        assertThat(graph_1_1.subGraphs().size(), is(1));

        Graph graph_2_1 = graph_1_1.subGraphs().iterator().next();

        assertThat(graph_2_1.nodeCount(), is(1));
        assertThat(graph_2_1.hasNode("mario"), is(true));
        assertThat(graph_2_1.hasLocalNodeAttribute(StdAttribute.label), is(true));
        assertThat(graph_2_1.<String>nodeAttribute(StdAttribute.label).get(graph.getNode("mario")), is("mushroomEater"));
    }

    @Test(expected = MalformedFileException.class)
    public void testGraphTooDeep() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("@attribute \t label ");
        lines.add("mario      \t redPlumber");
        lines.add("luigi      \t greenPlumber");
        lines.add("");
        lines.add("#edges");
        lines.add("@attribute \t @from \t @to   \t edgeWidth \t myMetric");
        lines.add("@types     \t       \t       \t           \t Double");
        lines.add("@default   \t       \t       \t 5         \t 4.5 ");
        lines.add("1          \t mario \t luigi \t 2");
        lines.add("2          \t mario \t luigi \t           \t 6");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("###graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("mario");
        lines.add("luigi");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);
    }

    @Test(expected = MalformedFileException.class)
    public void testTwiceRootGraph() {
        List<String> lines = new ArrayList<>();
        lines.add("#graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("@attribute \t label ");
        lines.add("mario      \t redPlumber");
        lines.add("luigi      \t greenPlumber");
        lines.add("");
        lines.add("#edges");
        lines.add("@attribute \t @from \t @to   \t edgeWidth \t myMetric");
        lines.add("@types     \t       \t       \t           \t Double");
        lines.add("@default   \t       \t       \t 5         \t 4.5 ");
        lines.add("1          \t mario \t luigi \t 2");
        lines.add("2          \t mario \t luigi \t           \t 6");
        lines.add("");
        lines.add("");
        lines.add("");
        lines.add("#graph");
        lines.add("");
        lines.add("#nodes");
        lines.add("mario");
        lines.add("luigi");

        OcoSaver saver = new OcoSaver();
        Graph graph = saver.read(lines);
    }
}
