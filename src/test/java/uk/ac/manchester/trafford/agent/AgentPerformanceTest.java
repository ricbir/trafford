package uk.ac.manchester.trafford.agent;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.GridRoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetwork;

public class AgentPerformanceTest {
	private static final Logger LOGGER = Logger.getLogger(AgentPerformanceTest.class.getName());

	private static final double AGENT_SPEED = 10;

	private static RoadNetwork network;

	@Before
	public void setUp() throws Exception {
		network = new GridRoadNetwork(8, 8, 100, AGENT_SPEED + 5);
	}

	// Currently 8 seconds on my machine
	@Test
	public void testPerformanceSamePath() throws PathNotFoundException, NodeNotFoundException {
		final int TOTAL_AGENTS = 100;
		Agent agentsRow0[] = new Agent[TOTAL_AGENTS];
		for (int i = 0; i < TOTAL_AGENTS; i++) {
			agentsRow0[i] = new Agent(network, "0.0", "0.7", AGENT_SPEED);
			for (int j = i; j > 0; j--) {
				agentsRow0[j].move();
			}
		}

		boolean done = false;
		while (!done) {
			for (int i = 0; i < TOTAL_AGENTS; i++) {
				done = agentsRow0[i].move();
			}
		}
	}

	// Currently 12 seconds on my machine
	@Test
	public void testPerformanceParallelPaths() throws PathNotFoundException, NodeNotFoundException {
		final int TOTAL_AGENTS = 100;
		Agent agentsRow0[] = new Agent[TOTAL_AGENTS / 4];
		Agent agentsRow1[] = new Agent[TOTAL_AGENTS / 4];
		Agent agentsRow2[] = new Agent[TOTAL_AGENTS / 4];
		Agent agentsRow3[] = new Agent[TOTAL_AGENTS / 4];
		for (int i = 0; i < TOTAL_AGENTS / 4; i++) {
			agentsRow0[i] = new Agent(network, "0.0", "0.7", AGENT_SPEED);
			agentsRow1[i] = new Agent(network, "1.0", "1.7", AGENT_SPEED);
			agentsRow2[i] = new Agent(network, "2.0", "2.7", AGENT_SPEED);
			agentsRow3[i] = new Agent(network, "3.0", "3.7", AGENT_SPEED);
			for (int j = i; j > 0; j--) {
				agentsRow0[j].move();
				agentsRow1[j].move();
				agentsRow2[j].move();
				agentsRow3[j].move();
			}
		}

		boolean done = false;
		while (!done) {
			for (int i = 0; i < TOTAL_AGENTS / 4; i++) {
				done = agentsRow0[i].move() && agentsRow1[i].move() && agentsRow2[i].move() && agentsRow3[i].move();
			}
		}
	}

}
