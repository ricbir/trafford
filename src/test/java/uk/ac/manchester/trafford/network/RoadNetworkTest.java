package uk.ac.manchester.trafford.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class RoadNetworkTest {
	private RoadNetwork network;

	@Mock
	Agent agent;

	@Mock
	Edge edge;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		network = new RoadNetwork();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetCoordinatesDiagonal() throws NodeNotFoundException {
		Vertex source = new Vertex(0, 0);
		Vertex target = new Vertex(200, 200);
		network.addVertex(source);
		network.addVertex(target);
		network.addEdge(source, target, edge);

		when(agent.getEdgePosition()).thenReturn(new EdgePosition(edge, Math.sqrt(2)));
		when(edge.getLength()).thenReturn(source.distance(target));

		assertEquals(new Vertex(100, 100), network.getCoordinates(agent));
	}

	@Test
	public void testGetCoordinatesXAxis() throws NodeNotFoundException {
		Vertex source = new Vertex(0, 0);
		Vertex target = new Vertex(200, 0);
		network.addVertex(source);
		network.addVertex(target);
		network.addEdge(source, target, edge);

		when(agent.getEdgePosition()).thenReturn(new EdgePosition(edge, 1));
		when(edge.getLength()).thenReturn(2.);

		assertEquals(new Vertex(100, 0), network.getCoordinates(agent));
	}

	@Test
	public void testGetCoordinatesYAxis() throws NodeNotFoundException {
		Vertex source = new Vertex(0, 0);
		Vertex target = new Vertex(0, 200);
		network.addVertex(source);
		network.addVertex(target);
		network.addEdge(source, target, edge);

		when(agent.getEdgePosition()).thenReturn(new EdgePosition(edge, 1));
		when(edge.getLength()).thenReturn(2.);

		assertEquals(new Vertex(0, 100), network.getCoordinates(agent));
	}
}
