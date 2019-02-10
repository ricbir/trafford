package uk.ac.manchester.trafford.exceptions;

import uk.ac.manchester.trafford.agent.Agent;

@SuppressWarnings("serial")
public class AlreadyAtDestinationException extends Exception {
	public AlreadyAtDestinationException(Agent agent) {
		super(agent + " is already at its destination");
	}
}
