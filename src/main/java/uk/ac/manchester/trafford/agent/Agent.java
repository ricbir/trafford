package uk.ac.manchester.trafford.agent;

import java.util.Iterator;
import java.util.Observable;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.Edge;
import uk.ac.manchester.trafford.network.Node;
import uk.ac.manchester.trafford.network.RoadNetwork;

public class Agent extends Observable {
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
	private static int agentId = 0;

	private RoadNetwork network;

	private String name;

	private String source;
	private String target;

	private double speed;
	private double speedPerTick;
	private double distanceFromEdgeOrigin = 0;
	private Edge currentEdge;
	private Edge nextEdge;

	private boolean destinationReached = false;

	GraphPath<Node, Edge> path;
	Iterator<Edge> edgeIterator;

	public Agent(String name, RoadNetwork network, String source, String target, double maxSpeed)
			throws PathNotFoundException, NodeNotFoundException {
		this.name = name;
		this.speed = maxSpeed;
		this.speedPerTick = maxSpeed / Constants.TICKS_PER_SECOND;
		this.network = network;
		this.source = source;
		this.target = target;

		updatePath();
		nextEdge();
	}

	public Agent(RoadNetwork network, String source, String target, double maxSpeed)
			throws PathNotFoundException, NodeNotFoundException {
		this("Agent #" + agentId++, network, source, target, maxSpeed);
	}

	private void nextEdge() {
		LOGGER.fine(String.format("Agent %s moving to next edge", this));

		if (currentEdge == null) {
			currentEdge = edgeIterator.next();
		} else {
			currentEdge.exit(this);
			currentEdge = nextEdge;
		}
		currentEdge.enter(this);
		nextEdge = edgeIterator.hasNext() ? edgeIterator.next() : null;
	}

	/**
	 * Moves the agent along the path.
	 * 
	 * @return whether or not the agent has reached its destination
	 */
	public boolean move() {
		if (destinationReached) {
			return true;
		}

		distanceFromEdgeOrigin += Math.min(getAvailableDistance(currentEdge.getFollowingAgent(this)), speedPerTick);

		double exceedingDistance = 0;
		double availableDistance;
		// while there's space left to move on to the next edge without reaching the
		// destination
		while ((exceedingDistance = distanceFromEdgeOrigin - currentEdge.getLength()) > Constants.SPATIAL_SENSITIVITY
				&& nextEdge != null && (availableDistance = getAvailableDistance(
						nextEdge.getFollowingAgent(this))) > Constants.SPATIAL_SENSITIVITY) {
			nextEdge();
			distanceFromEdgeOrigin = Math.min(availableDistance, exceedingDistance);
		}

		if (nextEdge == null && exceedingDistance >= 0) {
			currentEdge.exit(this);
			currentEdge = null;
			destinationReached = true;
			LOGGER.info(String.format("Agent %s has reached its destination", this));
		}

		return destinationReached;
	}

	private double getAvailableDistance(Agent followingAgent) {
		if (followingAgent == null) {
			return Double.MAX_VALUE;
		}
		double availableDistance = followingAgent.getDistance() - distanceFromEdgeOrigin - Constants.AGENT_DISTANCE;
		return availableDistance > Constants.SPATIAL_SENSITIVITY ? availableDistance : 0;
	}

	private void updatePath() throws PathNotFoundException, NodeNotFoundException {
		path = network.findPath(source, target);
		edgeIterator = path.getEdgeList().iterator();

		if (!edgeIterator.hasNext()) {
			throw new PathNotFoundException();
		}
	}

	public Edge getCurrentEdge() {
		return currentEdge;
	}

	public double getDistance() {
		return distanceFromEdgeOrigin;
	}

	@Override
	public String toString() {
		return name;
	}
}
