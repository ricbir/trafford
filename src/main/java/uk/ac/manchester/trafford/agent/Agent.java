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
import uk.ac.manchester.trafford.network.edge.EdgeAccessController;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class Agent {
	private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());
	private static int agentId = 0;

	private RoadNetwork network;

	private String name;

	private EdgePosition source;
	private EdgePosition target;

	// private List<Agent> watchlist = new ArrayList<>(5);
	private Agent leader = null;
	private Set<Agent> subscribers = new HashSet<>();
	private Set<Agent> subscribersRemoveSet = new HashSet<>();
	private Set<Agent> subscribersAddSet = new HashSet<>();

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

	private boolean shouldUpdatePath = true;

	private GraphPath<Point, Edge> path;
	private Iterator<Edge> edgeIterator;

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
		updateSubscribers();

		if (state == State.AT_DESTINATION) {
			throw new AlreadyAtDestinationException(this);
		}

		if (state == State.AT_SOURCE) {
			if (currentEdge.join(this, distanceOnCurrentEdge)) {
				state = State.TRAVELLING;
				LOGGER.fine(
						"Agent " + this + " has joined edge " + currentEdge + " at position " + distanceOnCurrentEdge);
			}
			return;
		}

		if (nextEdge != null && (leader == null || leader.currentEdge != currentEdge)) {
			setLeader(nextEdge.getLastAgent());
		}

		if (shouldUpdatePath) {
			executeUpdatePath();
		}

		updateSpeed();
		updatePosition();
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
			for (Agent subscriber : subscribers) {
				subscriber.leaderLeavingEdge(this);
			}
			LOGGER.fine("Agent " + this + " has reached its destination");
			return;
		}

		if (nextEdge != null && distanceOnCurrentEdge > currentEdge.getLength()) {
			distanceOnCurrentEdge -= currentEdge.getLength();
			setLeader(nextEdge.getLastAgent());
			changeEdge();
		}
	}

	private void updateSpeed() {
		speed += calculateSpeedDelta();
		// Avoid "bounce back" effect
		if (speed < 0.005) {
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
		try {
			currentEdge.enter(this);
		} catch (NullPointerException e) {
			LOGGER.severe(debugString());
		}

		nextEdge = edgeIterator.hasNext() ? edgeIterator.next() : null;

		for (Agent subscriber : subscribers) {
			subscriber.leaderLeavingEdge(this);
		}
	}

	/**
	 * Calculate the necessary speed adjustment. Based on the Intelligent Driver
	 * Model (https://en.wikipedia.org/wiki/Intelligent_driver_model)
	 * 
	 * @return The speed delta.
	 */
	private double calculateSpeedDelta() {
		double targetSpeed = maxSpeed < currentEdge.getSpeedLimit() ? maxSpeed : currentEdge.getSpeedLimit();
		double freeRoadTerm = Math.pow(speed / targetSpeed, 4);
		double leaderInteractionTerm = 0;
		double nextEdgeInteractionTerm = 0;
		if (leader != null) {
			double distanceToLeader = getDistance(leader);
			if (distanceToLeader < speed * 5 || distanceToLeader < Constants.MINIMUM_SPACING * 5) {
				leaderInteractionTerm = Math.pow(
						(Constants.MINIMUM_SPACING + speed * Constants.DESIRED_TIME_HEADWAY
								+ (speed * (speed - leader.speed))
										/ (2 * Math.sqrt(maxAcceleration * breakingDeceleration)))
								/ distanceToLeader,
						2);
			}
		}
		if (nextEdge != null) {
			double distanceToNextEdge = currentEdge.getLength() - distanceOnCurrentEdge;

			if (nextEdge.getAccessState() != EdgeAccessController.State.GREEN && distanceToNextEdge < 50) {
				if (nextEdge.getAccessState() == EdgeAccessController.State.RED
						|| distanceToNextEdge > speed * 3 - 30) {
					nextEdgeInteractionTerm = Math
							.pow((0.1 + (speed * speed) / (2 * Math.sqrt(maxAcceleration * breakingDeceleration)))
									/ distanceToNextEdge, 2);
				}
			} else {
				if (speed > nextEdge.getSpeedLimit() && distanceToNextEdge < 50) {
					nextEdgeInteractionTerm = Math.pow(((speed * (speed - nextEdge.getSpeedLimit()))
							/ (2 * Math.sqrt(maxAcceleration * breakingDeceleration))) / distanceToNextEdge, 2);
				}
			}
		}

		if (leaderInteractionTerm > nextEdgeInteractionTerm) {
			return maxAcceleration * (1 - freeRoadTerm - leaderInteractionTerm) / Constants.UPDATES_PER_SECOND;
		} else {
			return maxAcceleration * (1 - freeRoadTerm - nextEdgeInteractionTerm) / Constants.UPDATES_PER_SECOND;
		}

	}

	protected double getDistance(Agent a) {
		if (currentEdge == a.currentEdge) {
			return a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}

		if (nextEdge == a.currentEdge) {
			return currentEdge.getLength() + a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}
		return Integer.MAX_VALUE;
	}

	protected void leaderLeavingEdge(Agent agent) {
		if (leader == agent) {
			if (nextEdge != null) {
				setLeader(nextEdge.getLastAgent());
			} else {
				setLeader(null);
			}
		}
	}

	public void setLeader(Agent agent) {
		if (leader != agent) {
			if (leader != null) {
				leader.unsubscribe(this);
			}
			leader = agent;
			if (leader != null) {
				leader.subscribe(this);
			}
		}
	}

	protected void subscribe(Agent agent) {
		subscribersAddSet.add(agent);
	}

	protected void unsubscribe(Agent agent) {
		subscribersRemoveSet.add(agent);
	}

	private void updateSubscribers() {
		subscribers.removeAll(subscribersRemoveSet);
		subscribersRemoveSet.clear();

		subscribers.addAll(subscribersAddSet);
		subscribersAddSet.clear();
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

	public Agent getLeader() {
		return leader;
	}

	public EdgePosition getEdgePosition() {
		return new EdgePosition(currentEdge, distanceOnCurrentEdge);
	}

	public double getSpeed() {
		return speed;
	}

	@Override
	public String toString() {
		return name;
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
