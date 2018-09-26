package uk.ac.manchester.trafford.network;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.util.Convert;

public abstract class RoadNetwork {
	public static enum Type {
		Grid, Custom
	}

	private Type type;
	private Graph<Node, Edge> graph;

	/**
	 * Create a road network of given type and based on a graph.
	 * 
	 * @param type
	 * @param graph
	 */
	RoadNetwork(Type type, Graph<Node, Edge> graph) {
		this.type = type;
		this.graph = graph;
	}

	/**
	 * Get the type of the network.
	 * 
	 * @return The type of the network.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Get an edge of the network linking two nodes.
	 * 
	 * @param source Source node name.
	 * @param target Target node name.
	 * @return The edge.
	 */
	public Edge getEdge(String source, String target) {
		return graph.getEdge(new Node(source), new Node(target));
	}

	/**
	 * Add a node to the network.
	 * 
	 * @param name Unique name of the node.
	 */
	protected void addNode(String name) {
		graph.addVertex(new Node(name));
	}

	/**
	 * Add a new edge to the network.
	 * 
	 * @param source     Name of the source node.
	 * @param target     Name of the target node.
	 * @param length     Length of the edge, in meters.
	 * @param speedLimit Speed limit, in km/h.
	 */
	protected void addEdge(String source, String target, double length, double speedLimit) {
		Edge edge = new Edge(Convert.metersToMillimeters(length), Convert.kmphToMmps(speedLimit));
		graph.addEdge(new Node(source), new Node(target), edge);
		graph.setEdgeWeight(edge, length / edge.getSpeedLimit());
	}

	/**
	 * Get the set of edges in the network.
	 * 
	 * @return The set.
	 */
	protected Set<Edge> edgeSet() {
		return graph.edgeSet();
	}

	/**
	 * Get the set of nodes in the network.
	 * 
	 * @return The set.
	 */
	protected Set<Node> nodeSet() {
		return graph.vertexSet();
	}

	/**
	 * Find a path from one node to another in the network.
	 * 
	 * @param source The source node.
	 * @param target The target node.
	 * @return
	 * @throws NodeNotFoundException If either node is not in the graph.
	 * @throws PathNotFoundException If there is no path linking the two nodes.
	 */
	public GraphPath<Node, Edge> findPath(String source, String target)
			throws NodeNotFoundException, PathNotFoundException {
		GraphPath<Node, Edge> path;

		try {
			ShortestPathAlgorithm<Node, Edge> shortestPath = new DijkstraShortestPath<Node, Edge>(graph);
			path = shortestPath.getPath(new Node(source), new Node(target));
		} catch (IllegalArgumentException e) {
			throw new NodeNotFoundException(e);
		}

		if (path == null) {
			throw new PathNotFoundException();
		}

		return path;
	}
}
