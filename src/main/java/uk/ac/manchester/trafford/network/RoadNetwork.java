package uk.ac.manchester.trafford.network;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import uk.ac.manchester.trafford.Model;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.AlreadyAtDestinationException;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;

@SuppressWarnings("serial")
public class RoadNetwork extends DefaultDirectedGraph<Vertex, Edge> implements Model {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(RoadNetwork.class);

	private Set<Agent> agents = new HashSet<>();
	private Set<Agent> agentRemoveSet = new HashSet<>();
	private Set<Agent> agentAddSet = new HashSet<>();
	double averageCongestion = 0;

	private double agentSpeed = 30;
	private double agentSpeedVariability = 0.2;

	private Set<TimedTrafficLight> trafficLights = new HashSet<>();

	/**
	 * Create a road network of given type and based on a graph.
	 * 
	 */
	RoadNetwork() {
		super(Edge.class);
	}

	public void createAgent(EdgePosition source, EdgePosition target) {
		try {
			Agent agent = new Agent(this, source, target,
					agentSpeed + (Math.random() * agentSpeedVariability * agentSpeed));
			agentAddSet.add(agent);
		} catch (PathNotFoundException | NodeNotFoundException e) {
			LOGGER.warn("Could not create agent", e);
			;
		}
	}

	public void addTrafficLight(TimedTrafficLight trafficLight) {
		synchronized (trafficLight) {
			trafficLights.add(trafficLight);
		}
	}

	public Set<TimedTrafficLight> getTrafficLights() {
		return trafficLights;
	}

	/**
	 * Get a snapshot of the agents currently on the network, in the form of an
	 * array.
	 * 
	 * @return The snapshot.
	 */
	public synchronized Agent[] agentSetSnapshot() {
		return agents.toArray(new Agent[agents.size()]);
	}

	@Override
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
			averageCongestion += edge.getCongestionCoefficient() * edge.getLength();
			totalLength += edge.getLength();
		}
		averageCongestion /= totalLength;

		if (averageCongestion > 1. || averageCongestion < 0.) {
			LOGGER.warn("Meaningless congestion coefficient value: " + averageCongestion);
		}

		synchronized (trafficLights) {
			for (TimedTrafficLight subscriber : trafficLights) {
				subscriber.update();
			}
		}
	}

	public Vertex getCoordinates(Agent agent) {
		EdgePosition position = agent.getEdgePosition();
		Edge edge = position.getEdge();
		if (edge == null) {
			return null;
		}
		double distance = position.getDistance();
		Vertex source = getEdgeSource(edge);
		Vertex target = getEdgeTarget(edge);

		return new Vertex(distance / edge.getLength() * (target.getX() - source.getX()) + source.getX(),
				distance / edge.getLength() * (target.getY() - source.getY()) + source.getY());
	}

	public GraphPath<Vertex, Edge> getShortestPath(Vertex source, Vertex target) throws NodeNotFoundException {
		ShortestPathAlgorithm<Vertex, Edge> shortestPath = new DijkstraShortestPath<Vertex, Edge>(getWeightedNetwork());
		try {
			return shortestPath.getPath(source, target);
		} catch (IllegalArgumentException e) {
			throw new NodeNotFoundException(e);
		}
	}

	/**
	 * Get a view of the network with weights
	 * 
	 * @return
	 */
	private AsWeightedGraph<Vertex, Edge> getWeightedNetwork() {
		AsWeightedGraph<Vertex, Edge> weightedNetwork = new AsWeightedGraph<Vertex, Edge>(this,
				new Function<Edge, Double>() {

					@Override
					public Double apply(Edge edge) {

						return edge.getLength() / edge.getSpeedLimit() + edge.getAverageJourneyTime();
					}

				}, false, false);

		return weightedNetwork;
	}

	public double getAverageCongestion() {
		return averageCongestion;
	}

	public double getAgentSpeed() {
		return agentSpeed;
	}

	public void setAgentSpeed(double agentSpeed) {
		this.agentSpeed = agentSpeed;
	}

	public double getAgentSpeedVariability() {
		return agentSpeedVariability;
	}

	public void setAgentSpeedVariability(double agentSpeedVariability) {
		this.agentSpeedVariability = agentSpeedVariability;
	}

	public int getNumberOfAgents() {
		return agents.size();
	}
}
