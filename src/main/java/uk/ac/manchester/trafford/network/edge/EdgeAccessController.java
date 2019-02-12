package uk.ac.manchester.trafford.network.edge;

public interface EdgeAccessController {
	public enum State {
		TL_GREEN, TL_YELLOW, TL_RED, FREE
	}

	public State getState();
}
