package uk.ac.manchester.trafford.network.edge;

public class FreeFlowAccessController implements EdgeAccessController {

	@Override
	public State getState() {
		return State.FREE;
	}

}
