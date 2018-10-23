package uk.ac.manchester.trafford.network;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import uk.ac.manchester.trafford.Model;
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
	private Set<Agent> agentAddSet = new HashSet<>();
	double averageCongestion = 0;

	private Set<Model> subscribers = new HashSet<>();

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

	public void addAgent(Agent agent) {
		agentAddSet.add(agent);
	}

	public void subscribe(Model subscriber) {
		synchronized (subscribers) {
			subscribers.add(subscriber);
		}
	}

	public void unsubscribe(Model subscriber) {
		synchronized (subscribers) {
			subscribers.remove(subscriber);
		}
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
		agents.addAll(agentAddSet);
		agentAddSet.clear();
		for (Agent agent : agents) {
			try {
				agent.move();
			} catch (AlreadyAtDestinationException e) {
				agentRemoveSet.add(agent);
			} catch (PathNotFoundException e) {
				e.printStackTrace();
				agentRemoveSet.add(agent);
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
				agentRemoveSet.add(agent);
			}
		}
		agents.removeAll(agentRemoveSet);
		agentRemoveSet.clear();
		averageCongestion = 0;
		double totalLength = 0;
		for (Edge edge : edgeSet()) {
			averageCongestion += edge.getCongestion() * edge.getLength();
			totalLength += edge.getLength();
		}
		averageCongestion /= totalLength;

		synchronized (subscribers) {
			for (Model subscriber : subscribers) {
				subscriber.update();
			}
		}
	}

	public Point getCoordinates(Agent agent) {
		EdgePosition position = agent.getEdgePosition();
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

	public double getAverageCongestion() {
		return averageCongestion;
	}
}
