package uk.ac.manchester.trafford.agent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;

import uk.ac.manchester.trafford.Constants;
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
	private Agent leader = null;
	private Agent follower = null;

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

	private int journeyTimeCounter;

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
		this.currentEdge = null;
		this.distanceOnCurrentEdge = 0;
		this.journeyTimeCounter = (int) Math
				.round(source.getDistance() / source.getEdge().getSpeedLimit() * Constants.UPDATES_PER_SECOND);
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

		if (currentEdge == target.getEdge() && distanceOnCurrentEdge >= target.getDistance()) {
			leaveFlow();
			return;
		}

		if (state == State.AT_SOURCE) {
			joinFlow(source.getEdge(), source.getDistance());
			return;
		}

		if (shouldUpdatePath) {
			executeUpdatePath();
		}

		updateSpeed();
		updatePosition();
	}

	private void updatePosition() {
		distanceOnCurrentEdge += speed / Constants.UPDATES_PER_SECOND;
		journeyTimeCounter++;

		if (nextEdge != null && distanceOnCurrentEdge > currentEdge.getLength()) {
			distanceOnCurrentEdge -= currentEdge.getLength();
			currentEdge.setLastJourneyTime(journeyTimeCounter / (double) Constants.UPDATES_PER_SECOND);
			changeEdge();
			journeyTimeCounter = 0;
		}
	}

	private void updateSpeed() {
		speed += calculateSpeedDelta();
		// Avoid "bounce back" effect
		if (speed < 0.005) {
			speed = 0;
		}
	}

	private void joinFlow(Edge edge, double distance) {
		Agent newLeader = edge.getLastAgent();
		Agent newFollower = null;
		while (newLeader != null && newLeader.distanceOnCurrentEdge < distance) {
			newFollower = newLeader;
			newLeader = newLeader.leader;
		}
		if (newFollower == null) {
			edge.setLastAgent(this);
		}
		addToLeaderChain(newFollower, newLeader);

		currentEdge = edge;
		distanceOnCurrentEdge = distance;
		state = State.TRAVELLING;
		LOGGER.fine("Agent " + this + " has joined edge " + currentEdge + " at position " + distanceOnCurrentEdge);
	}

	private void leaveFlow() {
		if (follower == null) {
			currentEdge.setLastAgent(leader);
		}
		removeFromLeaderChain();
		currentEdge = null;
		state = State.AT_DESTINATION;
		LOGGER.fine("Agent " + this + " has reached its destination");
	}

	private void removeFromLeaderChain() {
		if (leader != null) {
			leader.follower = follower;
		}
		if (follower != null) {
			follower.leader = leader;
		}
		leader = null;
		follower = null;
	}

	private void addToLeaderChain(Agent newFollower, Agent newLeader) {
		if (newFollower != null) {
			newFollower.leader = this;
		}
		if (newLeader != null) {
			newLeader.follower = this;
		}
		follower = newFollower;
		leader = newLeader;
	}

	private void changeEdge() {
		LOGGER.finer(String.format("Agent %s moving to next edge", this));

		if (currentEdge.getLastAgent() == this) {
			currentEdge.setLastAgent(null);
		}

		removeFromLeaderChain();
		addToLeaderChain(null, nextEdge.getLastAgent());
		nextEdge.setLastAgent(this);

		currentEdge = nextEdge;
		nextEdge = edgeIterator.hasNext() ? edgeIterator.next() : null;
	}

	/**
	 * Calculate the necessary speed adjustment. Based on the Intelligent Driver
	 * Model.
	 * 
	 * @return The speed delta.
	 */
	private double calculateSpeedDelta() {
		double targetSpeed = maxSpeed < currentEdge.getSpeedLimit() ? maxSpeed : currentEdge.getSpeedLimit();
		double freeRoadTerm = Math.pow(speed / targetSpeed, 4);
		double leaderInteractionTerm = 0;
		double nextEdgeInteractionTerm = 0;

		Agent closestLeader = null;
		if (leader != null) {
			closestLeader = leader;
		} else if (nextEdge != null) {
			closestLeader = nextEdge.getLastAgent();
		}
		if (closestLeader != null) {
			leaderInteractionTerm = getIDMInteractionTerm(closestLeader.speed, getDistance(closestLeader));
		}

		if (nextEdge != null) {
			double distanceToNextEdge = currentEdge.getLength() - distanceOnCurrentEdge;

			switch (currentEdge.getAccessState()) {
			case TL_RED:
				nextEdgeInteractionTerm = getIDMInteractionTerm(0, distanceToNextEdge);
				break;
			case TL_YELLOW:
				if (distanceToNextEdge > speed * 3 - 30) {
					nextEdgeInteractionTerm = getIDMInteractionTerm(0, distanceToNextEdge);
					break;
				}
			case TL_GREEN:
			case FREE:
				if (speed > nextEdge.getSpeedLimit()) {
					nextEdgeInteractionTerm = getIDMInteractionTerm(nextEdge.getSpeedLimit(), distanceToNextEdge);
				}
				break;
			}
		}

		if (leaderInteractionTerm > nextEdgeInteractionTerm)

		{
			return maxAcceleration * (1 - freeRoadTerm - leaderInteractionTerm) / Constants.UPDATES_PER_SECOND;
		} else {
			return maxAcceleration * (1 - freeRoadTerm - nextEdgeInteractionTerm) / Constants.UPDATES_PER_SECOND;
		}

	}

	/**
	 * Calculates the interaction term for the Intelligent Driver Model
	 * (https://en.wikipedia.org/wiki/Intelligent_driver_model).
	 * 
	 * @param leaderSpeed
	 * @param distanceToLeader
	 * @return
	 */
	private double getIDMInteractionTerm(double leaderSpeed, double distanceToLeader) {
		return Math.pow((Constants.MINIMUM_SPACING + speed * Constants.DESIRED_TIME_HEADWAY
				+ (speed * (speed - leaderSpeed)) / (2 * Math.sqrt(maxAcceleration * breakingDeceleration)))
				/ distanceToLeader, 2);// TODO extract sqrt as constant for efficiency
	}

	/**
	 * Get the agent's distance from the specified agent
	 * 
	 * @param a The other agent
	 * @return Distance from the other agent. If the agent is not on current or next
	 *         edge, distance is Double.MAX_VALUE
	 */
	protected double getDistance(Agent a) {
		if (currentEdge == a.currentEdge) {
			return a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}

		if (nextEdge == a.currentEdge) {
			return currentEdge.getLength() + a.distanceOnCurrentEdge - distanceOnCurrentEdge;
		}
		return Double.MAX_VALUE;
	}

	private void executeUpdatePath() throws PathNotFoundException, NodeNotFoundException {
		path = network.getShortestPath(network.getEdgeTarget(source.getEdge()),
				network.getEdgeSource(target.getEdge()));

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

	Agent getLeader() {
		return leader;
	}

	Agent getFollower() {
		return follower;
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
