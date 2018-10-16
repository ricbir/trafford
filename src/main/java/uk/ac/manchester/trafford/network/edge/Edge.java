package uk.ac.manchester.trafford.network.edge;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.AgentNotOnEdgeException;

@SuppressWarnings("serial")
public class Edge extends DefaultWeightedEdge {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	private final double length;
	private double speedLimit = 200;

	private LinkedList<Agent> agents = new LinkedList<>();

	Edge(double length) {
		this.length = length;
	}

	/**
	 * Have an agent enter this edge.
	 * 
	 * @param agent The agent.
	 */
	public void enter(Agent agent) {
		LOGGER.fine("Agent " + agent + " entering edge " + this);
		agents.add(agent);
	}

	/**
	 * Have an agent exit this edge.
	 * 
	 * @param agent The agent.
	 */
	public void exit(Agent agent) throws AgentNotOnEdgeException {
		LOGGER.fine("Agent " + agent + " leaving edge " + this);
		if (!agents.remove(agent)) {
			throw new AgentNotOnEdgeException(agent, this);
		}
	}

	/**
	 * Try to join this edge at a certain distance.
	 * 
	 * @param agent
	 * @param distanceFromLaneStart
	 * @return Whether or not the operation was successful.
	 */
	public boolean join(Agent agent, double distanceFromLaneStart) {
		ListIterator<Agent> listIterator = agents.listIterator(agents.size());
		while (listIterator.hasPrevious()) {
			Agent follower = null;
			if ((follower = listIterator.previous()).getGraphPosition().getDistance() < distanceFromLaneStart) {
				follower.setLeader(agent);
				listIterator.next();
				listIterator.add(agent);
				if (listIterator.hasNext()) {
					agent.setLeader(listIterator.next());
				}
				return true;
			}
		}
		enter(agent);
		return true;
	}

	/**
	 * Returns the length of the edge in meters
	 * 
	 * @return1
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the speed limit of the edge in m/s
	 * 
	 * @return
	 */
	public double getSpeedLimit() {
		return speedLimit;
	}

	protected void setSpeedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
	}

	public Agent getLastAgent() {
		return agents.peekLast();
	}

	@Override
	public String toString() {
		return "{ source: " + getSource() + ", target: " + getTarget() + ", length: " + length + ", s/l: " + speedLimit
				+ " }";
	}
}
