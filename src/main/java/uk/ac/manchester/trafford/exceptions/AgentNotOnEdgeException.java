package uk.ac.manchester.trafford.exceptions;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.edge.Edge;

@SuppressWarnings("serial")
public class AgentNotOnEdgeException extends Exception {

	public AgentNotOnEdgeException(Agent agent, Edge edge) {
		super("Agent " + agent + " not found on edge " + edge);
	}

}
