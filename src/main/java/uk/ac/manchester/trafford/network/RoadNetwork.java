package uk.ac.manchester.trafford.network;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.AlreadyAtDestinationException;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

@SuppressWarnings("serial")
public class RoadNetwork extends DefaultDirectedWeightedGraph<Point, Edge> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(RoadNetwork.class.getName());

	private Set<Agent> agents = new HashSet<>();
	private Set<Agent> agentRemoveSet = new HashSet<>();

	/**
	 * Create a road network of given type and based on a graph.
	 * 
	 */
	RoadNetwork() {
		super(Edge.class);
		for (Edge edge : edgeSet()) {
			setEdgeWeight(edge, edge.getLength() / edge.getSpeedLimit());
		}
	}

	public synchronized void addAgents(Set<Agent> newAgents) {
		agents.addAll(newAgents);
	}

	/**
	 * Get a snapshot of the agents currently on the network, in the form of an
	 * array.
	 * 
	 * @return The snapshot.
	 */
	public Agent[] agentSetSnapshot() {
		return agents.toArray(new Agent[agents.size()]);
	}

	public synchronized void update() {
		for (Agent agent : agents) {
			try {
				agent.move();
			} catch (AlreadyAtDestinationException e) {
				agentRemoveSet.add(agent);
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
		}
		agents.removeAll(agentRemoveSet);
		agentRemoveSet.clear();
	}

	public Point getCoordinates(Agent agent) {
		EdgePosition position = agent.getGraphPosition();
		Edge edge = position.getEdge();
		if (edge == null) {
			return null;
		}
		double distance = position.getDistance();
		Point source = getEdgeSource(edge);
		Point target = getEdgeTarget(edge);

		return new Point(
				(int) Math.round(distance / edge.getLength() * (target.getX() - source.getX()) + source.getX()),
				(int) Math.round(distance / edge.getLength() * (target.getY() - source.getY()) + source.getY()));
	}

	public GraphPath<Point, Edge> getShortestPath(Point source, Point target) throws NodeNotFoundException {
		ShortestPathAlgorithm<Point, Edge> shortestPath = new DijkstraShortestPath<Point, Edge>(this);
		try {
			return shortestPath.getPath(source, target);
		} catch (IllegalArgumentException e) {
			throw new NodeNotFoundException(e);
		}
	}
}
