package uk.ac.manchester.trafford;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

/**
 * This class is based on the code found at
 * http://www.java-gaming.org/index.php?topic=24220.0
 * 
 * @author ricbir
 *
 */
public class SimulationController {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(SimulationController.class.getName());

	private static final double SIMULATION_SPEED = 1.;
	private final double TIME_BETWEEN_UPDATES = Constants.NANOSECONDS_PER_SECOND / (double) Constants.UPDATES_PER_SECOND
			/ SIMULATION_SPEED;
	private final double TARGET_TIME_BETWEEN_RENDERS = Constants.NANOSECONDS_PER_SECOND
			/ (double) Constants.RENDERS_PER_SECOND;

	// At the very most we will update the game this many times before a new render.
	// If you're worried about visual hitches more than perfect timing, set this to
	// 1.
	private final int MAX_UPDATES_BEFORE_RENDER = 5;
	// We will need the last update time.
	private double lastUpdateTime = System.nanoTime();
	// Store the last time we rendered.
	private double lastRenderTime = System.nanoTime();

	private boolean running = false;
	private boolean paused = false;

	private Renderer renderer;
	private RoadNetwork network;

	private Set<Agent> agentAddSet = new HashSet<>();

	SimulationController(Renderer renderer, RoadNetwork network) {
		this.renderer = renderer;
		this.network = network;

		renderer.setNetwork(network);
	}

	/**
	 * Starts a new thread and runs the game loop in it.
	 */
	public void run() {
		running = true;
		Thread loop = new Thread() {
			@Override
			public void run() {
				simulationLoop();
			}
		};
		loop.start();
	}

	/**
	 * Only run this in another Thread!
	 */
	private void simulationLoop() {

		// Simple way of finding FPS.
		int lastSecondTime = (int) (lastUpdateTime / Constants.NANOSECONDS_PER_SECOND);

		while (running) {
			double now = System.nanoTime();
			int updateCount = 0;

			if (!paused) {
				// Do as many game updates as we need to, potentially playing catchup.
				while (now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
					updateModel();
					lastUpdateTime += TIME_BETWEEN_UPDATES;
					updateCount++;
				}

				// If for some reason an update takes forever, we don't want to do an insane
				// number of catchups.
				// If you were doing some sort of game that needed to keep EXACT time, you would
				// get rid of this.
				if (now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
					lastUpdateTime = now - TIME_BETWEEN_UPDATES;
				}

				// Render. To do so, we need to calculate interpolation for a smooth render.
				float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES));
				renderer.render(interpolation);
				lastRenderTime = now;

				// Update the frames we got.
				int thisSecond = (int) (lastUpdateTime / Constants.NANOSECONDS_PER_SECOND);
				if (thisSecond > lastSecondTime) {
					lastSecondTime = thisSecond;
				}

				// Yield until it has been at least the target time between renders. This saves
				// the CPU from hogging.
				while (now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS
						&& now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
					Thread.yield();

					// This stops the app from consuming all your CPU. It makes this slightly less
					// accurate, but is worth it.
					// You can remove this line and it will still work (better), your CPU just
					// climbs on certain OSes.
					// FYI on some OS's this can cause pretty bad stuttering. Scroll down and have a
					// look at different peoples' solutions to this.
					try {
						Thread.sleep(1);
					} catch (Exception e) {
					}

					now = System.nanoTime();
				}
			}
		}
	}

	private void updateModel() {
		if (!agentAddSet.isEmpty()) {
			synchronized (agentAddSet) {
				network.addAgents(agentAddSet);
				agentAddSet.clear();
			}
		}
		network.update();
	}

	/**
	 * Add an agent to the model. The action will be performed before the next model
	 * update.
	 * 
	 * @param source
	 * @param target
	 * @param maxSpeed
	 */
	public void addAgent(EdgePosition source, EdgePosition target, double maxSpeed) {
		if (source.equals(target)) {
			return;
		}
		try {
			synchronized (agentAddSet) {
				agentAddSet.add(new Agent(network, source, target, maxSpeed));
			}
		} catch (PathNotFoundException | NodeNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
