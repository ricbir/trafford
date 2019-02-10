package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;

public class EdgeBuilderTest {

	private EdgeBuilder edgeBuilder;

	private Edge edge;

	private Point from = new Point(0, 0);
	private Point to = new Point(0, 10);

	@Mock
	private RoadNetwork network;

	@Mock
	private EdgeAccessController accessController;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		edgeBuilder = new EdgeBuilder(from, to);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEdgeBuilder() {
		edge = edgeBuilder.addToNetwork(network);
		assertEquals(from.distance(to), edge.getLength(), 0.01);
		assertEquals(200, edge.speedLimit, 0.01);
		assertTrue(edge.accessController instanceof FreeFlowAccessController);

		verify(network).addVertex(from);
		verify(network).addVertex(to);
		verify(network).addEdge(from, to, edge);
		verify(network).setEdgeWeight(edge, edge.getLength() / edge.speedLimit);
	}

	@Test
	public void testAccessController() {
		edge = edgeBuilder.accessController(accessController).addToNetwork(network);

		assertSame(accessController, edge.accessController);
	}

	@Test
	public void testSpeedLimit() {
		edge = edgeBuilder.speedLimit(10).addToNetwork(network);

		assertEquals(10, edge.speedLimit, 0.001);
	}

}
