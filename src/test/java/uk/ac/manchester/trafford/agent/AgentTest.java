package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class AgentTest {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(AgentTest.class.getName());
	private static final double AGENT_SPEED = 10;

	@Mock
	private RoadNetwork network;
	@Mock
	private GraphPath<Point, Edge> path;

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
	EdgePosition start;
	EdgePosition destination;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		edges.add(edge1);
		edges.add(edge2);
		edges.add(edge3);

		for (Edge edge : edges) {
			when(edge.getLength()).thenReturn(100.);
			when(edge.join(any(Agent.class), anyDouble())).thenReturn(true);
		}

		edgeList.add(edge2);
		when(path.getEdgeList()).thenReturn(edgeList);
		when(network.getShortestPath(any(), any())).thenReturn(path);

		start = new EdgePosition(edge1, 0);
		destination = new EdgePosition(edge3, 100);

		agent = new Agent(network, start, destination, AGENT_SPEED);
		leader = spy(new Agent(network, start, destination, AGENT_SPEED));
	}

	@Test
	public void testAgentInitialization() throws Exception {
		verify(network, times(2)).getShortestPath(any(), any());

		agent.move();
		verify(edge1).join(agent, 0);

		agent.move();
		verify(edge2).getLastAgent();
	}

	@Test
	public void testSetLeader() throws Exception {
		agent.setLeader(leader);
		verify(leader, times(1)).subscribe(agent);

		agent.setLeader(leader);
		verify(leader, never()).unsubscribe(agent);
		verify(leader, times(1)).subscribe(agent);

		Agent newLeader = spy(new Agent(network, start, destination, AGENT_SPEED));
		agent.setLeader(newLeader);
		verify(leader, times(1)).unsubscribe(agent);
		verify(newLeader, times(1)).subscribe(agent);

		agent.setLeader(null);
		verify(newLeader, times(1)).unsubscribe(agent);
	}

	@Test
	public void testLeaderLeavingEdge() {
		agent.setLeader(leader);

		agent.leaderLeavingEdge(leader);
		verify(leader).unsubscribe(agent);
	}

	@Test
	public void testLeaderLeavingEdgeWrongLeader() {
		agent.setLeader(null);

		agent.leaderLeavingEdge(leader);
		verify(leader, never()).unsubscribe(agent);
	}

	@Test
	public void testGetDistanceSameEdge() throws Exception {
		leader = new Agent(network, new EdgePosition(start.getEdge(), start.getDistance() + 10), destination,
				AGENT_SPEED);
		assertEquals(10, agent.getDistance(leader), 0.05);
	}

	@Test
	public void testGetDistanceNextEdge() throws Exception {
		leader = new Agent(network, new EdgePosition(edge2, 10), destination, AGENT_SPEED);
		when(edge1.getLength()).thenReturn(100.);
		assertEquals(110, agent.getDistance(leader), 0.05);
	}

	@Test
	public void testMoveNotFollowing() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		agent.move();
		agent.move();
		assertEquals(new EdgePosition(edge1, 0), agent.getEdgePosition());

		agent.move();
		assertEquals(
				new EdgePosition(edge1,
						Constants.AGENT_ACCELERATION / Constants.UPDATES_PER_SECOND / Constants.UPDATES_PER_SECOND),
				agent.getEdgePosition());
	}

	@Test
	public void testMoveToNextEdge() throws Exception {
		leader.move();
		agent.move();

		while (leader.getEdgePosition().getEdge() == edge1) {
			leader.move();
		}

		verify(edge1).exit(leader);
		verify(edge2).enter(leader);

	}

	@Test
	public void testMoveFollowing() throws Exception {
		leader = new Agent(network, start, destination, AGENT_SPEED / 10);
		agent.setLeader(leader);

		for (int i = 0; i < 100; i++) {
			leader.move();
			agent.move();
			assert (agent.getEdgePosition().getDistance() <= leader.getEdgePosition().getDistance());
		}
	}

	@Test
	public void testKeepMinimumDistance() throws Exception {
		leader = new Agent(network, new EdgePosition(start.getEdge(), start.getDistance() + 10), destination, 0);
		agent.setLeader(leader);

		for (int i = 0; i < 500; i++) {
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
}
