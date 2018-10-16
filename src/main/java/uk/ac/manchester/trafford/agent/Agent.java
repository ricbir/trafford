package uk.ac.manchester.trafford.agent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.exceptions.AgentNotOnEdgeException;
import uk.ac.manchester.trafford.exceptions.AlreadyAtDestinationException;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class Agent {
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
	private static int agentId = 0;

	private RoadNetwork network;

	private String name;

	private EdgePosition source;
	private EdgePosition target;

	// private List<Agent> watchlist = new ArrayList<>(5);
	private Agent next = null;
	private Set<Agent> subscribers = new HashSet<>();

	/** Current speed in m/s */
	private double speed;
	/** Maximum speed in m/s */
	private final double maxSpeed;

	/** Current speed in m/s^2 */
	private final double maxAcceleration;
	private final double breakingDeceleration;

	private double distanceOnCurrentEdge;
	private Edge currentEdge;
	private Edge nextEdge;

	private enum State {
		AT_SOURCE, TRAVELLING, AT_DESTINATION
	};

	private State state;

	private boolean shouldUpdateNextAgent = true;
	private boolean shouldUpdatePath = true;

	GraphPath<Point, Edge> path;
	Iterator<Edge> edgeIterator;

	public Agent(String name, RoadNetwork network, EdgePosition source, EdgePosition target, double maxSpeed)
			throws PathNotFoundException, NodeNotFoundException {
		this.name = name;
		this.speed = 0;
		this.maxSpeed = maxSpeed;
		this.maxAcceleration = Constants.AGENT_ACCELERATION;
		this.breakingDeceleration = Constants.AGENT_DECELERATION;
		this.network = network;
		this.source = source;
		this.target = target;
		this.shouldUpdateNextAgent = true;
		this.shouldUpdatePath = true;
		this.state = State.AT_SOURCE;
		this.currentEdge = source.getEdge();
		this.distanceOnCurrentEdge = source.getDistance();
		executeUpdatePath();
	}

	public Agent(RoadNetwork network, EdgePosition source, EdgePosition target, double maxSpeed)
			throws PathNotFoundException, NodeNotFoundException {
		this("Agent #" + agentId++, network, source, target, maxSpeed);
	}

	/**
	 * Moves the agent along the path.
	 * 
	 * @throws AlreadyAtDestinationException
	 * @throws NodeNotFoundException
	 * @throws PathNotFoundException
	 */
	public void move() throws AlreadyAtDestinationException, PathNotFoundException, NodeNotFoundException {
		if (state == State.AT_DESTINATION) {
			throw new AlreadyAtDestinationException(this);
		}

		if (state == State.AT_SOURCE) {
			if (currentEdge.join(this, distanceOnCurrentEdge)) {
				state = State.TRAVELLING;
				nextEdge.subscribe(this);
				executeUpdateNextAgent();
				LOGGER.fine(
						"Agent " + this + " has joined edge " + currentEdge + " at position " + distanceOnCurrentEdge);
			}
			return;
		}

		if (shouldUpdateNextAgent) {
			executeUpdateNextAgent();
		}

		if (shouldUpdatePath) {
			executeUpdatePath();
		}

		updatePosition();
		updateSpeed();
	}

	private void updatePosition() {
		distanceOnCurrentEdge += speed / Constants.UPDATES_PER_SECOND;

		if (currentEdge == target.getEdge() && distanceOnCurrentEdge >= target.getDistance()) {
			state = State.AT_DESTINATION;
			try {
				currentEdge.exit(this);
			} catch (AgentNotOnEdgeException e) {
				LOGGER.warning(e.getMessage());
			}
			LOGGER.fine("Agent " + this + " has reached its destination");
			return;
		}

		if (distanceOnCurrentEdge > currentEdge.getLength()) {
			distanceOnCurrentEdge -= currentEdge.getLength();
			changeEdge();
			for (Agent subscriber : subscribers) {
				subscriber.updateNextAgent();
			}
		}
	}

	private void updateSpeed() {
		speed += calculateSpeedDelta();
		if (speed < 0) {
			speed = 0;
		}
	}

	private void changeEdge() {
		LOGGER.fine(String.format("Agent %s moving to next edge", this));

		try {
			currentEdge.exit(this);
		} catch (AgentNotOnEdgeException e) {
			LOGGER.warning(e.getMessage());
		}
		currentEdge = nextEdge;
		currentEdge.enter(this);

		nextEdge = edgeIterator.hasNext() ? edgeIterator.next() : null;
		if (nextEdge != null) {
			nextEdge.subscribe(this);
		}
	}

	/**
	 * Calculate the necessary speed adjustment. Based on the Intelligent Driver
	 * Model (https://en.wikipedia.org/wiki/Intelligent_driver_model)
	 * 
	 * @return The speed delta.
	 */
	private double calculateSpeedDelta() {
		double freeRoadTerm = 1 - Math.pow(speed / maxSpeed, 4);
		double interactionTerm = 0;
		if (next != null) {
			double distance = getDistance(next);
			if (distance < speed * 5) {
				interactionTerm = Math.pow((Constants.MINIMUM_SPACING + speed * Constants.DESIRED_TIME_HEADWAY
						+ (speed * (speed - next.speed)) / (2 * Math.sqrt(maxAcceleration * breakingDeceleration)))
						/ distance, 2);
			}
		}
		return maxAcceleration * (freeRoadTerm - interactionTerm) / Constants.UPDATES_PER_SECOND;
	}

	private double getDistance(Agent a) {
		if (currentEdge == a.currentEdge) {
			return a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}

		if (nextEdge == a.currentEdge) {
			return currentEdge.getLength() + a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}
		return Integer.MAX_VALUE;
	}

	public void executeUpdateNextAgent() {
		if (next != null) {
			next.subscribers.remove(this);
			LOGGER.fine(this + " unsubscribed from " + next);
		}
		try {
			if ((next = currentEdge.getFollowingAgent(this)) == null && nextEdge != null) {
				next = nextEdge.getLastAgent();
			}
		} catch (AgentNotOnEdgeException e) {
			LOGGER.warning(e.getMessage());
		}
		if (next != null) {
			next.subscribers.add(this);
			LOGGER.fine(this + " subscribed to " + next);
		}

		shouldUpdateNextAgent = false;
	}

	public void updateNextAgent() {
		shouldUpdateNextAgent = true;
	}

	private void executeUpdatePath() throws PathNotFoundException, NodeNotFoundException {
		path = network.getShortestPath(network.getEdgeTarget(currentEdge), network.getEdgeSource(target.getEdge()));

		if (path == null) {
			throw new PathNotFoundException(source, target);
		}

		List<Edge> edgeList = new LinkedList<Edge>(path.getEdgeList());
		edgeList.add(target.getEdge());
		edgeIterator = edgeList.iterator();

		if (!edgeIterator.hasNext()) {
			throw new PathNotFoundException(source, target);
		}

		nextEdge = edgeIterator.next();

		shouldUpdatePath = false;
	}

	@Override
	public String toString() {
		return name;
	}

	public EdgePosition getGraphPosition() {
		return new EdgePosition(currentEdge, distanceOnCurrentEdge);
	}

	public String debugString() {
		if (currentEdge != null) {
			Point pos = network.getCoordinates(this);
			return String.format(
					"name: %s, source: %s, target: %s, currEdge: %s, dist: %3.2f, x: %d, y: %d, speed: %.2f", name,
					source, target, currentEdge, distanceOnCurrentEdge, pos.getX(), pos.getY(), speed);
		}
		return String.format("name: %s, source: %s, target: %s, currEdge: %s, dist: %3.2f, x: N/A, y: N/A, speed: %.2f",
				name, source, target, currentEdge, distanceOnCurrentEdge, speed);
	}
}
