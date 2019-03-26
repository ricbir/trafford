package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.agent.Agent;

public class EdgeTest {

	private static final int LENGTH = 100;
	private static final int SPEED_LIMIT = 20;
	private Edge edge;

	@Mock
	private Agent agent1;
	@Mock
	private Agent agent2;
	@Mock
	private Agent agent3;
	@Mock
	private Agent agent4;
	@Mock
	private Agent agent5;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}s

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testCongestionCoefficient() {
		edge = new Edge(LENGTH);
		edge.speedLimit = SPEED_LIMIT;
		double idealJourneyTime = LENGTH / SPEED_LIMIT;

		setAverageJourneyTime(edge, idealJourneyTime);
		assertEquals(0, edge.getCongestionCoefficient(), 0.005);

		setAverageJourneyTime(edge, idealJourneyTime * 4);
		assertEquals(0.5, edge.getCongestionCoefficient(), 0.005);

		setAverageJourneyTime(edge, idealJourneyTime * 200);
		assertEquals(0.99, edge.getCongestionCoefficient(), 0.005);
	}

	private void setAverageJourneyTime(Edge edge, double time) {
		for (int i = 0; i < Edge.JOURNEY_TIMES; i++) {
			edge.setLastJourneyTime(time);
		}
	}
}
