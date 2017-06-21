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
package ocotillo.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A graph.
 */
public class Graph {

    private final Map<String, Node> nodeMap = new HashMap<>();
    private final Map<String, Edge> edgeMap = new HashMap<>();

    private final Map<Node, Set<Edge>> incomingMap = new HashMap<>();
    private final Map<Node, Set<Edge>> outgoingMap = new HashMap<>();

    private Graph parentGraph;
    private final Set<Graph> subGraphs = new HashSet<>();

    private final Map<String, GraphAttribute<?>> graphAttributeMap = new HashMap<>();
    private final Map<String, NodeAttribute<?>> nodeAttributeMap = new HashMap<>();
    private final Map<String, EdgeAttribute<?>> edgeAttributeMap = new HashMap<>();

    private static long nodeIdIndex = 0;
    private static long edgeIdIndex = 0;

    private final Set<GraphObserver> observers = new HashSet<>();
    private final Set<Element> changedElements = new HashSet<>();
    private final Set<Graph> changedSubGraphs = new HashSet<>();
    private final Set<Attribute<?>> changedAttributes = new HashSet<>();
    private boolean bulkNotify = false;

    /**
     * Construct a graph.
     */
    public Graph() {
        this(null);
    }

    /**
     * Construct an empty subgraph.
     *
     * @param parentGraph the parent graph.
     */
    private Graph(Graph parentGraph) {
        this.parentGraph = parentGraph;
    }

    /**
     * Returns the graph nodes.
     *
     * @return the graph nodes.
     */
    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    /**
     * Creates and inserts a new node.
     *
     * @return the new node.
     */
    public Node newNode() {
        return newNode(null);
    }

    /**
     * Creates and inserts a new node.
     *
     * @param id the node id.
     * @return the new node.
     */
    public Node newNode(String id) {
        if (id == null) {
            do {
                id = (++nodeIdIndex) + "n";
            } while (rootGraph().hasNode(id));
        }

        Node node = new Node(id);
        add(node);
        return node;
    }

    /**
     * Checks if a node with given id is part of the graph.
     *
     * @param id the node id.
     * @return true if the node is contained in the graph, false otherwise.
     */
    public boolean hasNode(String id) {
        return nodeMap.containsKey(id);
    }

    /**
     * Returns a node with given id.
     *
     * @param id the node id.
     * @return the node having given id.
     */
    public Node getNode(String id) {
        return nodeMap.get(id);
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of graph nodes.
     */
    public int nodeCount() {
        return nodeMap.size();
    }

    /**
     * Returns the graph edges.
     *
     * @return the graph edges.
     */
    public Collection<Edge> edges() {
        return Collections.unmodifiableCollection(edgeMap.values());
    }

    /**
     * Creates and inserts a new edge.
     *
     * @param source the edge source.
     * @param target the edge target.
     * @return the new edge.
     */
    public Edge newEdge(Node source, Node target) {
        return newEdge(null, source, target);
    }

    /**
     * Creates and inserts a new edge.
     *
     * @param id the edge id.
     * @param source the edge source.
     * @param target the edge target.
     * @return the new edge.
     */
    public Edge newEdge(String id, Node source, Node target) {
        if (id == null) {
            do {
                id = (++edgeIdIndex) + "e";
            } while (rootGraph().hasEdge(id));
        }

        Edge edge = new Edge(id, source, target);
        add(edge);
        return edge;
    }

    /**
     * Checks if an edge with given id is part of the graph.
     *
     * @param id the edge id.
     * @return true if the edge is contained in the graph, false otherwise.
     */
    public boolean hasEdge(String id) {
        return edgeMap.containsKey(id);
    }

    /**
     * Returns an edge with given id.
     *
     * @param id the edge id.
     * @return the edge having given id.
     */
    public Edge getEdge(String id) {
        return edgeMap.get(id);
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return the number of graph edges.
     */
    public int edgeCount() {
        return edgeMap.size();
    }

    /**
     * Checks if an element is contained in the graph.
     *
     * @param element the element.
     * @return true if the element is contained in the graph, false otherwise.
     */
    public boolean has(Element element) {
        if (element instanceof Node) {
            return hasNode(element.id());
        }
        if (element instanceof Edge) {
            return hasEdge(element.id());
        }
        return false;
    }

    /**
     * Adds an already created element to a graph. When adding an element that
     * already exists, or when inserting an edge that does not have both
     * extremities in the graph, throws an exception.
     *
     * @param element the element to be added.
     */
    public void add(Element element) {
        addImplementation(element, false);
    }

    /**
     * Adds an already created element to a graph. When adding an element that
     * already exists, does nothing. When inserting an edge that does not have
     * both extremities in the graph, inserts these nodes in the graph.
     *
     * @param element the element to be added.
     */
    public void forcedAdd(Element element) {
        addImplementation(element, true);
    }

    /**
     * Private method that actually performs the operation add.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     */
    private void addImplementation(Element element, Boolean forced) {
        if (!shouldAddBePerformed(element, forced)) {
            return;
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            nodeMap.put(node.id(), node);
            incomingMap.put(node, new HashSet<Edge>());
            outgoingMap.put(node, new HashSet<Edge>());
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            addImplementation(edge.source(), true);
            addImplementation(edge.target(), true);

            edgeMap.put(edge.id(), edge);
            outgoingMap.get(edge.source()).add(edge);
            incomingMap.get(edge.target()).add(edge);
        }

        if (parentGraph != null) {
            parentGraph.addImplementation(element, true);
        }

        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Checks whether the add operation should be performed.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     * @return true if the operation should be performed, false or exception
     * otherwise.
     */
    private boolean shouldAddBePerformed(Element element, Boolean forced) {
        if (rootGraph().has(element)) {
            String id = element.id();
            Element rootElem = element instanceof Node ? rootGraph().getNode(id) : rootGraph().getEdge(id);
            if (rootElem != element) {
                throw new IllegalArgumentException("Adding a different node or edge instance for the same ID");
            }
        }

        if (has(element)) {
            if (forced) {
                return false;
            } else {
                throw new IllegalArgumentException("Adding an element that is already in the graph");
            }
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            if (!forced && (!has(edge.source()) || !has(edge.target()))) {
                throw new IllegalArgumentException("Adding an edge whose extremities are not in the graph");
            }
        }

        return true;
    }

    /**
     * Removes an element from a graph. When removing an element that is not in
     * the graph, or when removing a node that still has incident edges, throws
     * an exception.
     *
     * @param element the element to be removed.
     */
    public void remove(Element element) {
        removeImplementation(element, false);
    }

    /**
     * Removes an element from a graph. When removing an element that is not in
     * the graph, does nothing. When removing a node that still has incident
     * edges, removes all of them as well.
     *
     * @param element the element to be removed.
     */
    public void forcedRemove(Element element) {
        removeImplementation(element, true);
    }

    /**
     * Private method that actually performs the operation remove.
     *
     * @param element the element to be removed.
     * @param forced whether to force or not the operation.
     */
    private void removeImplementation(Element element, Boolean forced) {
        if (!shouldRemoveBePerformed(element, forced)) {
            return;
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            for (Edge edge : incomingMap.get(node)) {
                removeImplementation(edge, true);
            }
            for (Edge edge : outgoingMap.get(node)) {
                removeImplementation(edge, true);
            }
            for (NodeAttribute<?> attribute : localNodeAttributes().values()) {
                attribute.clear(node);
            }
            nodeMap.remove(node.id());
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            for (EdgeAttribute<?> attribute : localEdgeAttributes().values()) {
                attribute.clear(edge);
            }
            edgeMap.remove(edge.id());
            outgoingMap.get(edge.source()).remove(edge);
            incomingMap.get(edge.target()).remove(edge);
        }

        for (Graph subGraph : subGraphs) {
            subGraph.removeImplementation(element, true);
        }

        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Checks whether the remove operation should be performed.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     * @return true if the operation should be performed, false or exception
     * otherwise.
     */
    private boolean shouldRemoveBePerformed(Element element, Boolean forced) {
        if (!has(element)) {
            if (forced) {
                return false;
            } else {
                throw new IllegalArgumentException("Removing an element that is not in the graph.");
            }
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            if (!forced && degree(node) > 0) {
                throw new IllegalArgumentException("Removing a node that still has incident edges.");
            }
        }

        return true;
    }

    /**
     * Returns the number of incoming edges on a given node.
     *
     * @param node a node.
     * @return the incoming degree of the node.
     */
    public int inDegree(Node node) {
        return incomingMap.get(node).size();
    }

    /**
     * Returns the number of outgoing edges from a given node.
     *
     * @param node a node.
     * @return the outgoing degree of the node.
     */
    public int outDegree(Node node) {
        return outgoingMap.get(node).size();
    }

    /**
     * Returns the number of incoming and outgoing edges from a given node.
     *
     * @param node a node.
     * @return the total degree of the node.
     */
    public int degree(Node node) {
        return inDegree(node) + outDegree(node);
    }

    /**
     * Returns the incoming edges into a node.
     *
     * @param node a node.
     * @return the incoming edges into a node.
     */
    public Collection<Edge> inEdges(Node node) {
        return Collections.unmodifiableCollection(incomingMap.get(node));
    }

    /**
     * Returns the outgoing edges from a node.
     *
     * @param node a node.
     * @return the outgoing edges from a node
     */
    public Collection<Edge> outEdges(Node node) {
        return Collections.unmodifiableCollection(outgoingMap.get(node));
    }

    /**
     * Returns the incoming and outgoing edges from a node.
     *
     * @param node a node.
     * @return the incoming and outgoing edges from a node.
     */
    public Collection<Edge> inOutEdges(Node node) {
        Set<Edge> edges = new HashSet<>();
        edges.addAll(inEdges(node));
        edges.addAll(outEdges(node));
        return edges;
    }

    /**
     * Returns the edges leaving from an node and incoming into another.
     *
     * @param source the source of the edges.
     * @param target the target of the edges.
     * @return all the edges from source to target.
     */
    public Collection<Edge> fromToEdges(Node source, Node target) {
        Set<Edge> edges = new HashSet<>(outEdges(source));
        edges.retainAll(inEdges(target));
        return edges;
    }

    /**
     * Returns the parent graph.
     *
     * @return the parent graph, or null if the graph is the root.
     */
    public Graph parentGraph() {
        return parentGraph;
    }

    /**
     * Returns the root graph.
     *
     * @return the root graph, which is the graph itself if it is root.
     */
    public Graph rootGraph() {
        if (parentGraph != null) {
            return parentGraph.rootGraph();
        }
        return this;
    }

    /**
     * Returns the subgraphs of this graph.
     *
     * @return the subgraphs of this graph.
     */
    public Collection<Graph> subGraphs() {
        return Collections.unmodifiableCollection(subGraphs);
    }

    /**
     * Creates and inserts a new subgraph.
     *
     * @return the new subgraph.
     */
    public Graph newSubGraph() {
        Graph subGraph = new Graph(this);
        subGraphs.add(subGraph);

        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Creates and inserts a new subgraph.
     *
     * @param nodes the subset of parent nodes to be added.
     * @param edges the subset of parent edges to be added.
     * @return the new subgraph.
     */
    public Graph newSubGraph(Collection<Node> nodes, Collection<Edge> edges) {
        Graph subGraph = new Graph(this);
        subGraphs.add(subGraph);

        for (Node node : nodes) {
            subGraph.add(node);
        }
        for (Edge edge : edges) {
            subGraph.add(edge);
        }

        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Creates and inserts a new induced subgraph. It inserts the given nodes,
     * and all the parent edges between them.
     *
     * @param nodes the subset of parent nodes to be added.
     * @return the new induced subgraph.
     */
    public Graph newInducedSubGraph(Collection<Node> nodes) {
        Graph subGraph = new Graph(this);
        subGraphs.add(subGraph);

        for (Node node : nodes) {
            subGraph.add(node);
        }
        for (Node node : nodes) {
            for (Edge edge : outEdges(node)) {
                if (subGraph.has(edge.target())) {
                    subGraph.add(edge);
                }
            }
        }
        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Removes a subgraph from this graph.
     *
     * @param subGraph the subgraph to be removed.
     */
    public void removeSubGraph(Graph subGraph) {
        subGraph.parentGraph = null;
        subGraphs.remove(subGraph);

        changedSubGraphs.add(subGraph);
        notifyObservers();
    }

    /**
     * Returns the attribute map related to the specified type.
     *
     * @param type the attribute type.
     * @return the related map.
     */
    private Map<String, ? extends Attribute<?>> getAttributeMap(AttributeType type) {
        switch (type) {
            case graph:
                return graphAttributeMap;
            case node:
                return nodeAttributeMap;
            case edge:
                return edgeAttributeMap;
            default:
                throw new UnsupportedOperationException("The attribute type " + type.name() + " is not supported");
        }
    }

    /**
     * Retrieve an attribute from a graph hierarchy.
     *
     * @param type the type of attribute to be retrieved.
     * @param attrId the attribute id.
     * @return the desired attribute, or null if an attribute with given id does
     * not exist.
     */
    private Attribute<?> retrieveAttribute(AttributeType type, String attrId) {
        Map<String, ?> attributeMap = getAttributeMap(type);
        if (attributeMap.containsKey(attrId)) {
            return (Attribute) attributeMap.get(attrId);
        } else {
            return parentGraph != null ? parentGraph.retrieveAttribute(type, attrId) : null;
        }
    }

    /**
     * Retrieve an existing attribute from a graph hierarchy. Throws exception
     * if the attribute does not exist.
     *
     * @param type the type of attribute to be retrieved.
     * @param attrId the attribute id.
     * @return the desired attribute.
     */
    private Attribute<?> retrieveExistingAttribute(AttributeType type, String attrId) {
        Attribute<?> attribute = retrieveAttribute(type, attrId);
        if (attribute == null) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" does not exist");
        }
        return attribute;
    }

    /**
     * Returns all the attributes of a given type in the graph hierarchy.
     *
     * @param <T> the type of attribute returned.
     * @param type the attribute type to be retrieved.
     * @return the local attributes.
     */
    public <T> Map<String, T> attributes(AttributeType type) {
        Map<String, T> results = localAttributes(type);
        if (parentGraph != null) {
            results.putAll(parentGraph.<T>attributes(type));
        }
        return results;
    }

    /**
     * Returns all the graph attributes in the graph hierarchy.
     *
     * @return the graph attributes.
     */
    public Map<String, GraphAttribute<?>> graphAttributes() {
        return attributes(AttributeType.graph);
    }

    /**
     * Returns all the node attributes in the graph hierarchy.
     *
     * @return the node attributes.
     */
    public Map<String, NodeAttribute<?>> nodeAttributes() {
        return attributes(AttributeType.node);
    }

    /**
     * Returns all the edge attributes in the graph hierarchy.
     *
     * @return the edge attributes.
     */
    public Map<String, EdgeAttribute<?>> edgeAttributes() {
        return attributes(AttributeType.edge);
    }

    /**
     * Returns all the local attributes of a given type.
     *
     * @param <T> the type of attribute returned.
     * @param type the attribute type to be retrieved.
     * @return the local attributes.
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> localAttributes(AttributeType type) {
        return new HashMap<>((Map<String, T>) getAttributeMap(type));
    }

    /**
     * Returns all the local graph attributes.
     *
     * @return the local graph attributes.
     */
    public Map<String, GraphAttribute<?>> localGraphAttributes() {
        return localAttributes(AttributeType.graph);
    }

    /**
     * Returns all the local node attributes.
     *
     * @return the local node attributes.
     */
    public Map<String, NodeAttribute<?>> localNodeAttributes() {
        return localAttributes(AttributeType.node);
    }

    /**
     * Returns all the local edge attributes.
     *
     * @return the local edge attributes.
     */
    public Map<String, EdgeAttribute<?>> localEdgeAttributes() {
        return localAttributes(AttributeType.edge);
    }

    /**
     * Checks if the graph hierarchy has an attribute with given id.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasAttribute(AttributeType type, String attrId) {
        return retrieveAttribute(type, attrId) != null;
    }

    /**
     * Checks if the graph hierarchy has a graph attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasGraphAttribute(String attrId) {
        return hasAttribute(AttributeType.graph, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard graph attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasGraphAttribute(StdAttribute attribute) {
        return hasGraphAttribute(attribute.name());
    }

    /**
     * Checks if the graph hierarchy has a node attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasNodeAttribute(String attrId) {
        return hasAttribute(AttributeType.node, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard node attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasNodeAttribute(StdAttribute attribute) {
        return hasNodeAttribute(attribute.name());
    }

    /**
     * Checks if the graph hierarchy has an edge attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasEdgeAttribute(String attrId) {
        return hasAttribute(AttributeType.edge, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard edge attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasEdgeAttribute(StdAttribute attribute) {
        return hasEdgeAttribute(attribute.name());
    }

    /**
     * Checks if the graph has an attribute with given id.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalAttribute(AttributeType type, String attrId) {
        return getAttributeMap(type).containsKey(attrId);
    }

    /**
     * Checks if this graph has a local graph attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalGraphAttribute(String attrId) {
        return hasLocalAttribute(AttributeType.graph, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard graph attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalGraphAttribute(StdAttribute attribute) {
        return hasLocalGraphAttribute(attribute.name());
    }

    /**
     * Checks if this graph has a local node attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalNodeAttribute(String attrId) {
        return hasLocalAttribute(AttributeType.node, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard node attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalNodeAttribute(StdAttribute attribute) {
        return hasLocalNodeAttribute(attribute.name());
    }

    /**
     * Checks if this graph has a local edge attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalEdgeAttribute(String attrId) {
        return hasLocalAttribute(AttributeType.edge, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard edge attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalEdgeAttribute(StdAttribute attribute) {
        return hasLocalEdgeAttribute(attribute.name());
    }

    /**
     * Returns the attribute with given id. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    public Attribute<?> attribute(AttributeType type, String attrId) {
        return retrieveExistingAttribute(type, attrId);
    }

    /**
     * Returns the graph attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    @SuppressWarnings("unchecked")
    public <T> GraphAttribute<T> graphAttribute(String attrId) {
        if (!hasGraphAttribute(attrId)) {
            StdAttribute.createStdGraphAttribute(this, attrId);
        }
        return (GraphAttribute<T>) attribute(AttributeType.graph, attrId);
    }

    /**
     * Returns a standard graph attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for graphs) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> GraphAttribute<T> graphAttribute(StdAttribute attribute) {
        return graphAttribute(attribute.name());
    }

    /**
     * Returns the node attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the attribute type.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    @SuppressWarnings("unchecked")
    public <T> NodeAttribute<T> nodeAttribute(String attrId) {
        if (!hasNodeAttribute(attrId)) {
            StdAttribute.createStdNodeAttribute(this, attrId);
        }
        return (NodeAttribute<T>) attribute(AttributeType.node, attrId);
    }

    /**
     * Returns a standard node attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for nodes) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> NodeAttribute<T> nodeAttribute(StdAttribute attribute) {
        return nodeAttribute(attribute.name());
    }

    /**
     * Returns the edge attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the attribute type.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    @SuppressWarnings("unchecked")
    public <T> EdgeAttribute<T> edgeAttribute(String attrId) {
        if (!hasEdgeAttribute(attrId)) {
            StdAttribute.createStdEdgeAttribute(this, attrId);
        }
        return (EdgeAttribute<T>) attribute(AttributeType.edge, attrId);
    }

    /**
     * Returns a standard edge attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for edges) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> EdgeAttribute<T> edgeAttribute(StdAttribute attribute) {
        return edgeAttribute(attribute.name());
    }

    /**
     * Creates and insert an attribute in the root graph. If an attribute with
     * the same id already exists in the hierarchy, throws and exception.
     *
     * @param type the attribute type.
     * @param attrId the attribute id.
     * @param attribute the attribute to be assigned.
     */
    public void setAttribute(AttributeType type, String attrId, Attribute<?> attribute) {
        if (hasAttribute(type, attrId)) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" already exists");
        }
        rootGraph().setLocalAttribute(type, attrId, attribute);
    }

    /**
     * Creates and inserts a graph attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> GraphAttribute<T> newGraphAttribute(String attrId, T value) {
        GraphAttribute<T> attribute = new GraphAttribute<>(value);
        setAttribute(AttributeType.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard graph attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> GraphAttribute<T> newGraphAttribute(StdAttribute attribute, T value) {
        return newGraphAttribute(attribute.name(), value);
    }

    /**
     * Creates and inserts a node attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> NodeAttribute<T> newNodeAttribute(String attrId, T defaultValue) {
        NodeAttribute<T> attribute = new NodeAttribute<>(defaultValue);
        setAttribute(AttributeType.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard node attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> NodeAttribute<T> newNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newNodeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and inserts an edge attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> EdgeAttribute<T> newEdgeAttribute(String attrId, T defaultValue) {
        EdgeAttribute<T> attribute = new EdgeAttribute<>(defaultValue);
        setAttribute(AttributeType.edge, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard edge attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> EdgeAttribute<T> newEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newEdgeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Sets an attribute in this graph. If an attribute with the same id already
     * exists in this graph, throws and exception. Attributes created locally
     * are inaccessible at higher levels of the hierarchy and override higher
     * level attributes with the same id.
     *
     * @param type the attribute type.
     * @param attrId the attribute id.
     * @param attribute the attribute to be inserted.
     */
    @SuppressWarnings("unchecked")
    public void setLocalAttribute(AttributeType type, String attrId, Attribute<?> attribute) {
        Rules.checkId(attrId);
        Map<String, Attribute<?>> attributeMap = (Map<String, Attribute<?>>) getAttributeMap(type);
        if (attributeMap.containsKey(attrId)) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" already exists");
        }
        StdAttribute.checkStdAttributeCompatibility(attrId, attribute);
        attributeMap.put(attrId, attribute);
        changedAttributes.add(attribute);
        notifyObservers();
    }

    /**
     * Creates and inserts a graph attribute in this graph. Whenever an
     * attribute already exists at this level, and the value to be assigned has
     * a different type than before, throws and exception. Attributes created
     * locally are inaccessible at higher levels of the hierarchy and override
     * higher level attributes with the same id.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned.
     * @return the new attribute.
     */
    public <T> GraphAttribute<T> newLocalGraphAttribute(String attrId, T value) {
        GraphAttribute<T> attribute = new GraphAttribute<>(value);
        setLocalAttribute(AttributeType.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard graph attribute in this graph. Whenever an
     * attribute already exists at this level, and the value to be assigned has
     * a different type than before, throws and exception. Attributes created
     * locally are inaccessible at higher levels of the hierarchy and override
     * higher level attributes with the same id.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned.
     * @return the new attribute.
     */
    public <T> GraphAttribute<T> newLocalGraphAttribute(StdAttribute attribute, T value) {
        return newLocalGraphAttribute(attribute.name(), value);
    }

    /**
     * Creates and insert a node attribute in this graph. If an attribute with
     * the same id already exists in this graph, throws and exception.
     * Attributes created locally are inaccessible at higher levels of the
     * hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> NodeAttribute<T> newLocalNodeAttribute(String attrId, T defaultValue) {
        NodeAttribute<T> attribute = new NodeAttribute<>(defaultValue);
        setLocalAttribute(AttributeType.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and insert a standard node attribute in this graph. If an
     * attribute with the same id already exists in this graph, throws and
     * exception. Attributes created locally are inaccessible at higher levels
     * of the hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> NodeAttribute<T> newLocalNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalNodeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and insert an edge attribute in this graph. If an attribute with
     * the same id already exists in this graph, throws and exception.
     * Attributes created locally are inaccessible at higher levels of the
     * hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> EdgeAttribute<T> newLocalEdgeAttribute(String attrId, T defaultValue) {
        EdgeAttribute<T> attribute = new EdgeAttribute<>(defaultValue);
        setLocalAttribute(AttributeType.edge, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and insert a standard edge attribute in this graph. If an
     * attribute with the same id already exists in this graph, throws and
     * exception. Attributes created locally are inaccessible at higher levels
     * of the hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> EdgeAttribute<T> newLocalEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalEdgeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Removes an attribute with given id. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     */
    public void removeAttribute(AttributeType type, String attrId) {
        if (hasLocalAttribute(type, attrId)) {
            Map<String, ? extends Attribute<?>> attributeMap = getAttributeMap(type);
            Attribute<?> attributeRemoved = attributeMap.remove(attrId);
            changedAttributes.add(attributeRemoved);
            notifyObservers();
        } else {
            if (parentGraph != null) {
                parentGraph.removeAttribute(type, attrId);
            } else {
                throw new IllegalArgumentException("The attribute \"" + attrId + "\" does not exist");
            }
        }
    }

    /**
     * Removes a graph attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeGraphAttribute(String attrId) {
        removeAttribute(AttributeType.graph, attrId);
    }

    /**
     * Removes a standard graph attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeGraphAttribute(StdAttribute attribute) {
        removeGraphAttribute(attribute.name());
    }

    /**
     * Removes a node attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeNodeAttribute(String attrId) {
        removeAttribute(AttributeType.node, attrId);
    }

    /**
     * Removes a standard node attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeNodeAttribute(StdAttribute attribute) {
        removeNodeAttribute(attribute.name());
    }

    /**
     * Removes an edge attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeEdgeAttribute(String attrId) {
        removeAttribute(AttributeType.edge, attrId);
    }

    /**
     * Removes a standard edge attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeEdgeAttribute(StdAttribute attribute) {
        removeEdgeAttribute(attribute.name());
    }

    /**
     * Register a graph observer.
     *
     * @param observer the observer.
     */
    protected void registerObserver(GraphObserver observer) {
        observers.add(observer);
    }

    /**
     * Unregister a graph observer.
     *
     * @param observer the observer.
     */
    protected void unregisterObserver(GraphObserver observer) {
        observers.remove(observer);
    }

    /**
     * Starts a bulk notification. All notifications are suspended until the
     * bulk notification end command, at which point all notifications are
     * transmitted in block.
     */
    public void startBulkNotification() {
        bulkNotify = true;
    }

    /**
     * Ends a bulk notification. All notifications stacked up during the bulk
     * notification interval are transmitted in block.
     */
    public void stopBulkNotification() {
        bulkNotify = false;
        notifyObservers();
    }

    /**
     * Notifies the observers.
     */
    private void notifyObservers() {
        if (bulkNotify) {
            return;
        }

        for (GraphObserver observer : observers) {
            if (!changedElements.isEmpty()) {
                observer.updateElements(Collections.unmodifiableCollection(changedElements));
            }
            if (!changedSubGraphs.isEmpty()) {
                observer.updateSubGraphs(Collections.unmodifiableCollection(changedSubGraphs));
            }
            if (!changedAttributes.isEmpty()) {
                observer.updateAttributes(Collections.unmodifiableCollection(changedAttributes));
            }
        }

        changedElements.clear();
        changedSubGraphs.clear();
        changedAttributes.clear();
    }
}
