package uk.ac.manchester.trafford.network.edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
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

	private Set<Agent> subscribers = new HashSet<>();
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
		subscribers.remove(agent);
		for (Agent subscriber : subscribers) {
			subscriber.updateNextAgent();
		}
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
			if (listIterator.previous().getGraphPosition().getDistance() < distanceFromLaneStart) {
				listIterator.add(agent);
				subscribers.remove(agent);
				if (listIterator.hasPrevious()) {
					listIterator.previous().updateNextAgent();
				}
				return true;
			}
		}
		enter(agent);
		return true;
	}

	public Agent getFollowingAgent(Agent agent) throws AgentNotOnEdgeException {
		Iterator<Agent> iterator = agents.descendingIterator();
		try {
			while (iterator.next() != agent)
				;
		} catch (NoSuchElementException e) {
			throw new AgentNotOnEdgeException(agent, this);
		}
		return iterator.hasNext() ? iterator.next() : null;
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

	public void subscribe(Agent agent) {
		subscribers.add(agent);
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
