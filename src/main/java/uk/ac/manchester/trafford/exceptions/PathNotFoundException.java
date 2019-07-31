package uk.ac.manchester.trafford.exceptions;

import uk.ac.manchester.trafford.agent.Position;

@SuppressWarnings("serial")
public class PathNotFoundException extends Exception {

	public PathNotFoundException(Position source, Position target) {
		super("Could not find path from " + source + " to " + target);
	}

}
