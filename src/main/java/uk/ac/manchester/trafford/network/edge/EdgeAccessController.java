package uk.ac.manchester.trafford.network.edge;

public interface EdgeAccessController {
	public enum State {
		GREEN, YELLOW, RED
	}

	public State getState();
}
