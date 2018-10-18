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
	 * @param distanceFromEdgeStart
	 * @return Whether or not the operation was successful.
	 */
	public boolean join(Agent agent, double distanceFromEdgeStart) {
		ListIterator<Agent> listIterator = agents.listIterator();
		while (listIterator.hasNext()) {
			Agent follower = null;
			if ((follower = listIterator.next()).getEdgePosition().getDistance() < distanceFromEdgeStart) {
				follower.setLeader(agent);
				listIterator.previous();
				listIterator.add(agent);
				listIterator.previous();
				if (listIterator.hasPrevious()) {
					agent.setLeader(listIterator.previous());
				}
				return true;
			}
		}
		if (listIterator.hasPrevious()) {
			agent.setLeader(listIterator.previous());
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

	public void checkListIntegrity() {
		Agent lastAgent = null;
		for (Agent agent : agents) {
			if (lastAgent != null && lastAgent.getLeader() != agent) {
				LOGGER.warning("Agent " + agent + " should be leader of agent " + lastAgent + ", but agent "
						+ lastAgent.getLeader() + " was.");
				System.out.println("Agent " + agent + " should be leader of agent " + lastAgent + ", but agent "
						+ lastAgent.getLeader() + " was.");
			}
		}
	}

	@Override
	public String toString() {
		return "{ source: " + getSource() + ", target: " + getTarget() + ", length: " + length + ", s/l: " + speedLimit
				+ " }";
	}
}
