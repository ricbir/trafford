package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Before;
import org.junit.Test;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.CustomRoadNetwork;
import uk.ac.manchester.trafford.network.Edge;
import uk.ac.manchester.trafford.network.GridRoadNetwork;
import uk.ac.manchester.trafford.network.Node;
import uk.ac.manchester.trafford.network.RoadNetwork;

public class AgentTest {
	private static final Logger LOGGER = Logger.getLogger(AgentTest.class.getName());

	private static final double AGENT_SPEED = 10;

	private static RoadNetwork network;

	private Agent agent;
	private Edge edge;

	@Before
	public void setUp() throws Exception {
		network = new GridRoadNetwork(8, 8, 100, AGENT_SPEED + 5);
	}

	@Test
	public void testMoveSpeed() throws PathNotFoundException, NodeNotFoundException {
		agent = new Agent(network, "0.0", "0.1", AGENT_SPEED);
		assertEquals(0, agent.getDistance(), Constants.SPATIAL_SENSITIVITY);
		for (int i = 0; i < Constants.TICKS_PER_SECOND; i++) {
			agent.move();
		}
		assertEquals(AGENT_SPEED, agent.getDistance(), Constants.SPATIAL_SENSITIVITY);
	}

	@Test
	public void testMoveToNextEdge() throws PathNotFoundException, NodeNotFoundException {
		agent = new Agent(network, "0.0", "0.2", AGENT_SPEED);
		edge = agent.getCurrentEdge();

		assertEquals(network.getEdge("0.0", "0.1"), agent.getCurrentEdge());

		for (int i = 0; i < Constants.TICKS_PER_SECOND * edge.getLength() / AGENT_SPEED + 1; i++) {
			agent.move();
		}
		assertEquals(network.getEdge("0.1", "0.2"), agent.getCurrentEdge());
		assertTrue(agent.getDistance() < 1);
	}

	@Test
	public void testMoveToFollowingNextEdge() throws PathNotFoundException, NodeNotFoundException {
		agent = new Agent(network, "0.0", "0.3", AGENT_SPEED);
		edge = agent.getCurrentEdge();

		assertEquals(network.getEdge("0.0", "0.1"), agent.getCurrentEdge());

		for (int i = 0; i < Constants.TICKS_PER_SECOND
				* (network.getEdge("0.0", "0.1").getLength() + network.getEdge("0.1", "0.2").getLength()) / AGENT_SPEED
				+ 1; i++) {
			agent.move();
		}
		assertEquals(network.getEdge("0.2", "0.3"), agent.getCurrentEdge());
		assertTrue(agent.getDistance() < 1);
	}

	@Test
	public void testDestinationNotReached() throws PathNotFoundException, NodeNotFoundException {
		agent = new Agent(network, "0.0", "0.1", AGENT_SPEED);
		edge = agent.getCurrentEdge();

		assertEquals(0, agent.getDistance(), Constants.SPATIAL_SENSITIVITY);

		for (int i = 0; i < Math.round(Constants.TICKS_PER_SECOND * edge.getLength() / AGENT_SPEED) - 1; i++) {
			assertFalse("End reached after " + i / Constants.TICKS_PER_SECOND * AGENT_SPEED + " m", agent.move());
		}
	}

	@Test
	public void testDestinationReached() throws PathNotFoundException, NodeNotFoundException {
		agent = new Agent(network, "0.0", "0.1", AGENT_SPEED);
		edge = agent.getCurrentEdge();

		assertEquals(0, agent.getDistance(), Constants.SPATIAL_SENSITIVITY);

		for (int i = 0; i < Math.round(Constants.TICKS_PER_SECOND * edge.getLength() / AGENT_SPEED); i++) {
			agent.move();
		}
		assertTrue(agent.move());
	}

	@Test
	public void testNodeDoesNotExist() {
		try {
			agent = new Agent(network, "0.0", "not_a_node", AGENT_SPEED);
			fail("Exception not thrown");
		} catch (NodeNotFoundException e) {

		} catch (Exception e) {
			fail("Expected " + NodeNotFoundException.class + ", got " + e);
		}
	}

	@Test
	public void testPathNotFound() {
		DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<Node, Edge>(Edge.class);
		graph.addVertex(new Node("a"));
		graph.addVertex(new Node("b"));
		network = new CustomRoadNetwork(graph);

		try {
			agent = new Agent(network, "a", "b", AGENT_SPEED);
			fail("Exception not thrown");
		} catch (PathNotFoundException e) {

		} catch (Exception e) {
			fail("Expected " + PathNotFoundException.class + ", got " + e);
		}
	}

	@Test
	public void testAchillesAndTheTortoise() throws PathNotFoundException, NodeNotFoundException {
		Agent tortoise = new Agent("tortoise", network, "0.0", "7.7", AGENT_SPEED / 2);
		tortoise.move();
		Agent achilles = new Agent("achilles", network, "0.0", "7.7", AGENT_SPEED);

		while (!tortoise.move()) {
			assertFalse("Achilles beat the tortoise. Zeno is not impressed.", achilles.move());
		}
	}

}
