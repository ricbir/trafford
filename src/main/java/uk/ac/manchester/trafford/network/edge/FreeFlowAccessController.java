package uk.ac.manchester.trafford.network.edge;

import javafx.beans.value.ObservableValue;

public class FreeFlowAccessController implements EdgeAccessController {

	@Override
	public State getState() {
		return State.FREE;
	}

	@Override
	public ObservableValue<State> getObservableState() {
		// TODO Auto-generated method stub
		return null;
	}

}
