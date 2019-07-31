package uk.ac.manchester.trafford.network.edge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.Model;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

public class TimedTrafficLight implements Model {

	private int greenUpdates;
	private int yellowUpdates;
	private int greenSeconds;
	private int yellowSeconds;

	/**
	 * Unique id for the traffic light
	 */
	protected int mId = 0;

	RoadNetwork network;

	private List<AccessController> controllers;
	private Iterator<AccessController> controllerIterator;
	private AccessController currentController;
	private int timer = 0;

	public TimedTrafficLight(int id, int greenSeconds, int yellowSeconds, int stages, RoadNetwork network) {
		mId = id;
		if (stages < 2) {
			throw new IllegalArgumentException("Timed traffic light must have at least two stages");
		}
		if (greenSeconds < 0 || yellowSeconds < 0) {
			throw new IllegalArgumentException("Seconds must be non-negative integers");
		}

		this.network = network;
		network.addTrafficLight(this);

		this.greenSeconds = greenSeconds;
		this.yellowSeconds = yellowSeconds;

		this.greenUpdates = greenSeconds * Constants.UPDATES_PER_SECOND;
		this.yellowUpdates = yellowSeconds * Constants.UPDATES_PER_SECOND;

		controllers = new ArrayList<>(stages);
		for (int i = 0; i < stages; i++) {
			controllers.add(new AccessController(State.TL_RED));
		}
		this.controllerIterator = Iterables.cycle(controllers).iterator();
		this.currentController = controllerIterator.next();
	}

	public int getId() {
		return mId;
	}

	public void SetGreenSeconds(int seconds) {
		greenSeconds = seconds;
		greenUpdates = greenSeconds * Constants.UPDATES_PER_SECOND;
	}

	public void SetYellowSeconds(int seconds) {
		yellowSeconds = seconds;
		yellowUpdates = yellowSeconds * Constants.UPDATES_PER_SECOND;
	}

	public int GetGreenSeconds() {
		return greenSeconds;
	}

	public int GetYellowSeconds() {
		return yellowSeconds;
	}

	public int stages() {
		return controllers.size();
	}

	public EdgeAccessController getController(int index) {
		return controllers.get(index);
	}

	@Override
	public void update() {
		if (--timer > 0) {
			return;
		}

		switch (currentController.getState()) {
		case TL_YELLOW:
			currentController.setState(State.TL_RED);
			currentController = controllerIterator.next();
		case TL_RED:
			currentController.setState(State.TL_GREEN);
			timer = greenUpdates;
			break;
		case TL_GREEN:
			currentController.setState(State.TL_YELLOW);
			timer = yellowUpdates;
			break;
		default:
			currentController.setState(State.TL_RED);
			break;
		}
	}

	public void setGreenTime(double seconds) {
		this.greenSeconds = (int) seconds;
		this.greenUpdates = (int) Math.round(seconds * Constants.UPDATES_PER_SECOND);
	}

	public void setYellowTime(double seconds) {
		this.yellowSeconds = (int) seconds;
		this.yellowUpdates = (int) Math.round(seconds * Constants.UPDATES_PER_SECOND);
	}

	public class AccessController implements EdgeAccessController {
		ObjectProperty<State> observableState;

		private AccessController(State initialState) {
			this.observableState = new SimpleObjectProperty<>(initialState);
		}

		private void setState(State state) {
			observableState.set(state);
		}

		@Override
		public State getState() {
			return observableState.get();
		}

		@Override
		public ObservableValue<State> getObservableState() {
			return observableState;
		}

	}
}
