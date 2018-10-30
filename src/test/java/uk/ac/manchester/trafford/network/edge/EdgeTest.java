package uk.ac.manchester.trafford.network.edge;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.agent.Agent;

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

}
