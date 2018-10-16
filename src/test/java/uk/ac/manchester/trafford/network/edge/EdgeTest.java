package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.AgentNotOnEdgeException;

public class EdgeTest {

	Edge edge;

	@Mock
	Agent agent1;
	@Mock
	Agent agent2;

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

	@Test
	public void testSubscribe() {

	}

}
