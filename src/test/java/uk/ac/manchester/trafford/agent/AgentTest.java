package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
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
	}

	@Test
	public void testAgentInitialization() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		verify(network).getShortestPath(any(), any());
		agent.move();

		verify(edge1).join(agent, 0);
		verify(edge2).subscribe(agent);
		verify(edge1).getFollowingAgent(agent);
		verify(edge2).getLastAgent();
	}

	@Test
	public void testUpdateNextAgentThisEdge() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		Agent leader = new Agent(network, start, destination, AGENT_SPEED);
		agent.move();
		leader.move();

		reset(edge1);
		reset(edge2);

		when(edge1.getFollowingAgent(agent)).thenReturn(leader);

		agent.updateNextAgent();
		agent.move();

		verify(edge1).getFollowingAgent(agent);
		verify(edge2, never()).getLastAgent();
	}

	@Test
	public void testUpdateNextAgentNextEdge() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		Agent leader = new Agent(network, start, destination, AGENT_SPEED);
		agent.move();
		leader.move();

		reset(edge1);
		reset(edge2);

		when(edge2.getLastAgent()).thenReturn(leader);

		agent.updateNextAgent();
		agent.move();

		verify(edge1).getFollowingAgent(agent);
		verify(edge2).getLastAgent();
	}

	@Test
	public void testMoveNotFollowing() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		agent.move();
		agent.move();
		assertEquals(new EdgePosition(edge1, 0), agent.getGraphPosition());

		agent.move();
		assertEquals(
				new EdgePosition(edge1,
						Constants.AGENT_ACCELERATION / Constants.UPDATES_PER_SECOND / Constants.UPDATES_PER_SECOND),
				agent.getGraphPosition());
	}

	@Test
	public void testMoveToNextEdge() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED);
		Agent follower = spy(new Agent(network, start, destination, AGENT_SPEED));
		when(edge1.getFollowingAgent(follower)).thenReturn(agent);

		agent.move();
		follower.move();

		while (agent.getGraphPosition().getEdge() == edge1) {
			agent.move();
		}

		verify(edge1).exit(agent);
		verify(edge2).enter(agent);
		verify(follower).updateNextAgent();

	}

	@Test
	public void testMoveFollowing() throws Exception {
		agent = new Agent(network, start, destination, AGENT_SPEED / 10);
		Agent follower = spy(new Agent(network, start, destination, AGENT_SPEED));

		when(edge1.getFollowingAgent(follower)).thenReturn(agent);
		follower.updateNextAgent();

		for (int i = 0; i < 100; i++) {
			agent.move();
			follower.move();
			assert (follower.getGraphPosition().getDistance() <= agent.getGraphPosition().getDistance());
		}
	}
}
