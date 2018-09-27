package uk.ac.manchester.trafford.network;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.manchester.trafford.agent.Agent;

public class Edge extends DefaultWeightedEdge {

	private static final long serialVersionUID = -4142216540556919212L;
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	private double length;
	private double speedLimit;

	public NavigableSet<Agent> agents = new TreeSet<>(new AgentPositionComparator());

	/**
	 * @param length     The length of the edge in mm
	 * @param speedLimit The speed limit in mm/s
	 * 
	 */
	public Edge(int length, int speedLimit) {
		this.length = length;
		this.speedLimit = speedLimit;
	}

	/**
	 * Have an agent enter this edge.
	 * 
	 * @param agent The agent.
	 */
	public void enter(Agent agent) {
		agents.add(agent);
		LOGGER.fine(String.format("Agent %s has entered edge %s", agent, this));
	}

	/**
	 * Have an agent exit this edge.
	 * 
	 * @param agent The agent.
	 */
	public void exit(Agent agent) {
		agents.remove(agent);
		LOGGER.fine(String.format("Agent %s has left edge %s", agent, this));
	}

	/**
	 * Update the agent's position in the set.
	 * 
	 * @param Agent The agent
	 */
	public void updatePosition(Agent agent) {
		Agent higherAgent = agents.higher(agent);
		if (higherAgent != null && higherAgent.getDistance() < agent.getDistance()) {
			agents.remove(agent);
			agents.add(agent);
		}
		LOGGER.finer(String.format("Agent %s has updated its position on edge %s. New position: %d", agent, this,
				agent.getDistance()));
	}

	public Agent getFollowingAgent(Agent agent) {
		return agents.higher(agent);
	}

	/**
	 * 
	 * Comparator for agents, which compares them by their distance along the edge.
	 *
	 */
	private class AgentPositionComparator implements Comparator<Agent> {
		@Override
		public int compare(Agent agent1, Agent agent2) {
			if (agent1 == agent2) {
				return 0;
			}
			return (int) Math.round(agent2.getDistance() - agent1.getDistance() * 1000);
		}
	}

	/**
	 * Returns the length of the edge in meters
	 * 
	 * @return
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

}
