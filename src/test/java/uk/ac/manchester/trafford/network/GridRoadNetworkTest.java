package uk.ac.manchester.trafford.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import util.Convert;

public class GridRoadNetworkTest {

	private GridRoadNetwork network;
	private static final int HEIGHT = 8;
	private static final int WIDTH = 8;
	private static final int EDGE_LENGTH = 100;
	private static final int SPEED_LIMIT = 50;
	private static final double DELTA_MAX = 0.0005;

	@Before
	public void setUp() throws Exception {
		network = new GridRoadNetwork(HEIGHT, WIDTH, EDGE_LENGTH, SPEED_LIMIT);
	}

	@Test
	public void testGridRoadNetwork() {
		Set<Node> nodeSet = network.nodeSet();
		Set<Edge> edgeSet = network.edgeSet();

		// Test correct number of nodes and edges
		assertEquals(HEIGHT * WIDTH, nodeSet.size());
		assertEquals(((WIDTH - 1) * HEIGHT + (HEIGHT - 1) * WIDTH) * 2, edgeSet.size());

		// Test nodes are connected correctly and of the correct length and speed limit
		for (Node node : nodeSet) {
			String[] coordinates = node.getName().split("\\.");
			int i = Integer.parseInt(coordinates[0]);
			int j = Integer.parseInt(coordinates[1]);

			if (i > 0) {
				Edge upEdge1 = network.getEdge(node.getName(), (i - 1) + "." + j);
				Edge upEdge2 = network.getEdge((i - 1) + "." + j, node.getName());
				assertNotNull("Missing edge: " + node.getName() + " -> " + (i - 1) + "." + j, upEdge1);
				assertNotNull("Missing edge: " + (i - 1) + "." + j + " -> " + node.getName(), upEdge2);
				assertEquals(Convert.metersToMillimeters(EDGE_LENGTH), upEdge1.getLength(), DELTA_MAX);
				assertEquals(Convert.metersToMillimeters(EDGE_LENGTH), upEdge2.getLength(), DELTA_MAX);
				assertEquals(Convert.kmphToMmps(SPEED_LIMIT), upEdge1.getSpeedLimit(), DELTA_MAX);
				assertEquals(Convert.kmphToMmps(SPEED_LIMIT), upEdge2.getSpeedLimit(), DELTA_MAX);
			}

			if (j > 0) {
				Edge leftEdge1 = network.getEdge(node.getName(), i + "." + (j - 1));
				Edge leftEdge2 = network.getEdge(i + "." + (j - 1), node.getName());
				assertNotNull("Missing edge: " + node.getName() + " -> " + i + "." + (j - 1), leftEdge1);
				assertNotNull("Missing edge: " + i + "." + (j - 1) + " -> " + node.getName(), leftEdge2);
				assertEquals(Convert.metersToMillimeters(EDGE_LENGTH), leftEdge1.getLength(), DELTA_MAX);
				assertEquals(Convert.metersToMillimeters(EDGE_LENGTH), leftEdge2.getLength(), DELTA_MAX);
				assertEquals(Convert.kmphToMmps(SPEED_LIMIT), leftEdge1.getSpeedLimit(), DELTA_MAX);
				assertEquals(Convert.kmphToMmps(SPEED_LIMIT), leftEdge2.getSpeedLimit(), DELTA_MAX);
			}

		}
	}

	@Test
	public void testGetType() {
		assertEquals(RoadNetwork.Type.Grid, network.getType());
	}

}
