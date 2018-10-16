package uk.ac.manchester.trafford.exceptions;

import uk.ac.manchester.trafford.network.edge.EdgePosition;

@SuppressWarnings("serial")
public class PathNotFoundException extends Exception {

	public PathNotFoundException(EdgePosition source, EdgePosition target) {
		super("Could not find path from " + source + " to " + target);
	}

}
