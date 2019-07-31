package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Vertex;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class AgentTest {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(AgentTest.class.getName());
	private static final double AGENT_SPEED = 10;

	@Mock
	private RoadNetwork network;
	@Mock
	private GraphPath<Vertex, Edge> path;

	@Mock
	private Edge edge1;
	@Mock
	private Edge edge2;
	@Mock
	private Edge edge3;

	private List<Edge> edgeList = new ArrayList<>(3);
	private Set<Edge> edges = new HashSet<>();

	private Agent agent;
	private Agent leader;
	private Agent follower;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		edges.add(edge1);
		edges.add(edge2);
		edges.add(edge3);

		for (Edge edge : edges) {
			when(edge.getLength()).thenReturn(100.);
			when(edge.getSpeedLimit()).thenReturn(200.);
			when(edge.getAccessState()).thenReturn(EdgeAccessController.State.TL_GREEN);
		}

		edgeList.add(edge2);
		when(path.getEdgeList()).thenReturn(edgeList);
		when(network.getShortestPath(any(), any())).thenReturn(path);
	}

	@Test
	public void testAgentInitialization() throws Exception {
		Vertex pathSource = Mockito.mock(Vertex.class);
		Vertex pathTarget = Mockito.mock(Vertex.class);
		when(network.getEdgeTarget(edge1)).thenReturn(pathSource);
		when(network.getEdgeSource(edge3)).thenReturn(pathTarget);

		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 100), 0);

		verify(network).getShortestPath(pathSource, pathTarget);
		assertNull(agent.getEdgePosition().getEdge());
	}

	@Test
	public void testGetDistanceSameEdge() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge1, 10), new EdgePosition(edge3, 10), AGENT_SPEED);

		leader.move();
		agent.move();

		assertEquals(10, agent.getDistance(leader), 0.05);
	}

	@Test
	public void testGetDistanceNextEdge() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge2, 10), new EdgePosition(edge3, 10), AGENT_SPEED);
		when(edge1.getLength()).thenReturn(100.);

		leader.move();
		agent.move();

		assertEquals(110, agent.getDistance(leader), 0.05);
	}

	@Test
	public void testGetDistanceBeyondNextEdge() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge3, 10), new EdgePosition(edge3, 20), AGENT_SPEED);
		when(edge1.getLength()).thenReturn(100.);

		leader.move();
		agent.move();

		assertEquals(Double.MAX_VALUE, agent.getDistance(leader), 0.05);
	}

	@Test
	public void testJoinFlowBehind() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 20), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge1, 30), new EdgePosition(edge3, 10), AGENT_SPEED);

		leader.move();
		when(edge1.getLastAgent()).thenReturn(leader);
		agent.move();

		assertSame(leader, agent.getLeader());
		assertNull(agent.getFollower());
		assertSame(agent, leader.getFollower());
		assertNull(leader.getLeader());

		verify(edge1).setLastAgent(leader);
		verify(edge1).setLastAgent(agent);
	}

	@Test
	public void testJoinFlowAhead() throws Exception {
		follower = new Agent(network, new EdgePosition(edge1, 10), new EdgePosition(edge3, 10), AGENT_SPEED);
		agent = new Agent(network, new EdgePosition(edge1, 20), new EdgePosition(edge3, 10), AGENT_SPEED);

		follower.move();
		when(edge1.getLastAgent()).thenReturn(follower);
		agent.move();

		assertSame(agent, follower.getLeader());
		assertNull(follower.getFollower());
		assertSame(follower, agent.getFollower());
		assertNull(agent.getLeader());

		verify(edge1).setLastAgent(follower);
		verify(edge1, never()).setLastAgent(agent);
	}

	@Test
	public void testJoinFlowMiddle() throws Exception {
		follower = new Agent(network, new EdgePosition(edge1, 10), new EdgePosition(edge3, 10), AGENT_SPEED);
		agent = new Agent(network, new EdgePosition(edge1, 20), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge1, 30), new EdgePosition(edge3, 10), AGENT_SPEED);

		follower.move();
		when(edge1.getLastAgent()).thenReturn(follower);
		leader.move();
		agent.move();

		assertNull(follower.getFollower());
		assertSame(agent, follower.getLeader());
		assertSame(follower, agent.getFollower());
		assertSame(leader, agent.getLeader());
		assertSame(agent, leader.getFollower());
		assertNull(leader.getLeader());

		verify(edge1).setLastAgent(follower);
		verify(edge1, never()).setLastAgent(agent);
		verify(edge1, never()).setLastAgent(leader);
	}

	@Test
	public void testMoveNotFollowing() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);

		agent.move();
		assertEquals(new EdgePosition(edge1, 0), agent.getEdgePosition());

		agent.move();
		assertEquals(
				new EdgePosition(edge1,
						Constants.AGENT_ACCELERATION / Constants.UPDATES_PER_SECOND / Constants.UPDATES_PER_SECOND),
				agent.getEdgePosition());
	}

	@Ignore
	@Test
	public void testMoveToNextEdge() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 10), AGENT_SPEED);
		leader.move();
		agent.move();

		int timeout = 3000;
		while (leader.getEdgePosition().getEdge() == edge1) {
			leader.move();
			if (timeout-- == 0) {
				fail("Operation timed out");
			}
		}

	}

	@Ignore
	@Test
	public void testMoveFollowing() throws Exception {
		// leader = new Agent(network, start, destination, AGENT_SPEED / 10);
		// agent.setLeader(leader);

		for (int i = 0; i < 100; i++) {
			leader.move();
			agent.move();
			assertTrue(
					"follower position: " + agent.getEdgePosition().getDistance() + ", leader position: "
							+ leader.getEdgePosition().getDistance(),
					agent.getEdgePosition().getDistance() <= leader.getEdgePosition().getDistance());
		}
	}

	@Ignore
	@Test
	public void testKeepMinimumDistance() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 80), new EdgePosition(edge3, 100), AGENT_SPEED);
		leader = new Agent(network, new EdgePosition(edge1, 80), new EdgePosition(edge3, 100), AGENT_SPEED);
		// start.getDistance() + 10), destination, 0);
		// agent.setLeader(leader);

		for (int i = 0; i < Constants.UPDATES_PER_SECOND * 10; i++) {
			agent.move();
			double agentDistance = agent.getEdgePosition().getDistance();
			double leaderDistance = leader.getEdgePosition().getDistance();
			double distanceBetweenAgents = leaderDistance - agentDistance;
			assertTrue(
					"Follower position: " + agentDistance + ", leader position: " + leaderDistance + ", distance: "
							+ distanceBetweenAgents + " > " + Constants.MINIMUM_SPACING,
					distanceBetweenAgents > Constants.MINIMUM_SPACING);
		}
	}

	@Test
	public void testSlowDownForSpeedLimit() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 0), new EdgePosition(edge3, 100), AGENT_SPEED);

		when(edge2.getSpeedLimit()).thenReturn(AGENT_SPEED - 2);
		while (agent.getEdgePosition().getDistance() < edge1.getLength() - 1) {
			agent.move();
		}
		assertEquals(edge1, agent.getEdgePosition().getEdge());
		assertEquals(AGENT_SPEED - 2, agent.getSpeed(), 0.5);
	}

	@Ignore
	@Test
	public void testStopAtRedLight() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 90), new EdgePosition(edge3, 100), AGENT_SPEED);

		when(edge1.getAccessState()).thenReturn(EdgeAccessController.State.TL_RED);
		for (int i = 0; i < Constants.UPDATES_PER_SECOND * 10; i++) {
			agent.move();
		}
		assertEquals(edge1, agent.getEdgePosition().getEdge());
		assertEquals(edge1.getLength(), agent.getEdgePosition().getDistance(), 1);
	}

	@Ignore
	@Test
	public void testStopAtYellowLight() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 80), new EdgePosition(edge3, 100), AGENT_SPEED);

		when(edge1.getAccessState()).thenReturn(EdgeAccessController.State.TL_YELLOW);
		for (int i = 0; i < Constants.UPDATES_PER_SECOND * 10; i++) {
			agent.move();
		}
		assertEquals(edge1, agent.getEdgePosition().getEdge());
		assertEquals(edge1.getLength(), agent.getEdgePosition().getDistance(), 1);
	}

	@Test
	public void testRunYellowLight() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 80), new EdgePosition(edge3, 100), AGENT_SPEED);

		when(edge1.getAccessState()).thenReturn(EdgeAccessController.State.TL_GREEN);
		for (int i = 0; i < Constants.UPDATES_PER_SECOND * 10; i++) {
			agent.move();
			if (agent.getEdgePosition().getDistance() > 99)
				when(edge2.getAccessState()).thenReturn(EdgeAccessController.State.TL_YELLOW);
		}
		assertEquals(edge2, agent.getEdgePosition().getEdge());
	}

	@Test
	public void testRunGreenLight() throws Exception {
		agent = new Agent(network, new EdgePosition(edge1, 80), new EdgePosition(edge3, 100), AGENT_SPEED);

		when(edge1.getAccessState()).thenReturn(EdgeAccessController.State.TL_GREEN);
		for (int i = 0; i < Constants.UPDATES_PER_SECOND * 5; i++) {
			agent.move();
		}
		assertEquals(edge2, agent.getEdgePosition().getEdge());
	}
}
