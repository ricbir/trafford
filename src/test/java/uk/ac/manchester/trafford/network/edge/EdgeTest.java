package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.AgentNotOnEdgeException;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class EdgeTest {

	Edge edge;

	@Mock
	Agent agent1;
	@Mock
	Agent agent2;
	@Mock
	Agent agent3;
	@Mock
	Agent agent4;
	@Mock
	Agent agent5;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		edge = new Edge(100);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testJoinEmptyEdge() {
		edge.join(agent1, 10);

		verify(agent1, never()).setLeader(any());
	}

	@Test
	public void testJoinOneAhead() {
		when(agent2.getEdgePosition()).thenReturn(new EdgePosition(edge, 20));
		edge.enter(agent2);

		edge.join(agent1, 10);

		verify(agent1).setLeader(agent2);
		verify(agent2, never()).setLeader(any());
	}

	@Test
	public void testJoinOneBehind() {
		when(agent1.getEdgePosition()).thenReturn(new EdgePosition(edge, 10));
		edge.enter(agent1);

		edge.join(agent2, 20);

		verify(agent1).setLeader(agent2);
		verify(agent2, never()).setLeader(any());
	}

	@Test
	public void testJoinManyAhead() {
		when(agent2.getEdgePosition()).thenReturn(new EdgePosition(edge, 20));
		when(agent3.getEdgePosition()).thenReturn(new EdgePosition(edge, 30));
		edge.enter(agent3);
		edge.enter(agent2);

		edge.join(agent1, 10);

		verify(agent1).setLeader(agent2);
		verify(agent2, never()).setLeader(any());
		verify(agent3, never()).setLeader(any());
	}

	@Test
	public void testJoinManyBehind() {
		when(agent1.getEdgePosition()).thenReturn(new EdgePosition(edge, 10));
		when(agent2.getEdgePosition()).thenReturn(new EdgePosition(edge, 20));
		edge.enter(agent2);
		edge.enter(agent1);

		edge.join(agent3, 30);

		verify(agent1, never()).setLeader(any());
		verify(agent2).setLeader(agent3);
		verify(agent3, never()).setLeader(any());
	}

	@Test
	public void testJoinBetweenMany() {
		when(agent1.getEdgePosition()).thenReturn(new EdgePosition(edge, 10));
		when(agent2.getEdgePosition()).thenReturn(new EdgePosition(edge, 20));
		when(agent4.getEdgePosition()).thenReturn(new EdgePosition(edge, 40));
		when(agent5.getEdgePosition()).thenReturn(new EdgePosition(edge, 50));
		edge.enter(agent5);
		edge.enter(agent4);
		edge.enter(agent2);
		edge.enter(agent1);

		edge.join(agent3, 30);

		verify(agent1, never()).setLeader(any());
		verify(agent2).setLeader(agent3);
		verify(agent3).setLeader(agent4);
		verify(agent4, never()).setLeader(any());
		verify(agent5, never()).setLeader(any());
	}

	@Test
	public void testEnter() {
		edge.enter(agent1);
	}

	@Test
	public void testExit() {
		edge.enter(agent1);
		try {
			edge.exit(agent1);
		} catch (AgentNotOnEdgeException e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	@Test
	public void testExitWithoutEnter() {
		try {
			edge.exit(agent1);
			fail("AgentNotOnEdgeException not thrown");
		} catch (AgentNotOnEdgeException e) {

		}
	}

	@Test
	public void testGetLastAgent() {
		assertNull(edge.getLastAgent());
		edge.enter(agent1);
		assertSame(agent1, edge.getLastAgent());
	}

}
