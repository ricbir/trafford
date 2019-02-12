package uk.ac.manchester.trafford.network.edge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.Model;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

public class TimedTrafficLight implements Model {

	private final int greenUpdates;
	private final int yellowUpdates;

	RoadNetwork network;

	private List<TimedTrafficLightAccessController> controllers;
	private Iterator<TimedTrafficLightAccessController> controllerIterator;
	private TimedTrafficLightAccessController currentController;
	private int timer = 0;

	public TimedTrafficLight(int greenSeconds, int yellowSeconds, int stages, RoadNetwork network) {
		if (stages < 2) {
			throw new IllegalArgumentException("Timed traffic light must have at least two stages");
		}
		if (greenSeconds < 0 || yellowSeconds < 0) {
			throw new IllegalArgumentException("Seconds must be non-negative integers");
		}

		this.network = network;
		network.subscribe(this);

		this.greenUpdates = greenSeconds * Constants.UPDATES_PER_SECOND;
		this.yellowUpdates = yellowSeconds * Constants.UPDATES_PER_SECOND;

		controllers = new ArrayList<>(stages);
		for (int i = 0; i < stages; i++) {
			controllers.add(new TimedTrafficLightAccessController(State.TL_RED));
		}
		this.controllerIterator = Iterables.cycle(controllers).iterator();
		this.currentController = controllerIterator.next();
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

		switch (currentController.state) {
		case TL_YELLOW:
			currentController.state = State.TL_RED;
			currentController = controllerIterator.next();
		case TL_RED:
			currentController.state = State.TL_GREEN;
			timer = greenUpdates;
			break;
		case TL_GREEN:
			currentController.state = State.TL_YELLOW;
			timer = yellowUpdates;
		}
	}

	private class TimedTrafficLightAccessController implements EdgeAccessController {
		State state;

		private TimedTrafficLightAccessController(State initialState) {
			this.state = initialState;
		}

		@Override
		public State getState() {
			return state;
		}

	}
}
