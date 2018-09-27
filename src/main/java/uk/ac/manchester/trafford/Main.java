package uk.ac.manchester.trafford;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.GridRoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.util.Convert;

public class Main {

	public static final Logger GLOBAL_LOGGER = Logger.getLogger(Main.class.getPackage().getName());
	public static final Handler CONSOLE_HANDLER = new ConsoleHandler();

	public static void main(String[] args) {
		GLOBAL_LOGGER.setLevel(Level.FINE);
		GLOBAL_LOGGER.setUseParentHandlers(false);
		GLOBAL_LOGGER.addHandler(CONSOLE_HANDLER);
		CONSOLE_HANDLER.setLevel(Level.ALL);

		RoadNetwork network = new GridRoadNetwork(8, 8, 100, 50);
		Agent agent1 = null;
		// Run an agent from node 0.0 to node 7.7 at 50kmph
		try {
			agent1 = new Agent(network, "0.0", "7.7", Convert.metersToMillimeters(Convert.kmphToMps(50)));
		} catch (PathNotFoundException | NodeNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (int tick = 0; tick < Constants.TICKS_PER_SECOND * 30000; tick++) {
			if (agent1.move()) {
				GLOBAL_LOGGER.info("Destination reached!");
				System.exit(0);
			}
		}
		GLOBAL_LOGGER.info("Timeout. Agent at edge " + agent1.getCurrentEdge() + " distance " + agent1.getDistance());
		System.exit(1);
	}
}
