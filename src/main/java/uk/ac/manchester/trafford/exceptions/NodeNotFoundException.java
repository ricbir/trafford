package uk.ac.manchester.trafford.exceptions;

public class NodeNotFoundException extends Exception {

	public NodeNotFoundException(IllegalArgumentException e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 743563162214211315L;

}
