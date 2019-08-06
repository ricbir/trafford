package uk.ac.manchester.trafford.network.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;
import uk.ac.manchester.trafford.network.SegmentConnection;

public class SimpleGridRoadNetworkFactoryTest {

	private static final int ROWS = 5;
	private static final int COLUMNS = 7;
	private static final double LENGTH = 100;

	private SimpleGridRoadNetworkFactory factory;
	private RoadNetwork network;

	@Test
	public void testNetworkSizeNoUTurns() {
		factory = new SimpleGridRoadNetworkFactory(COLUMNS, ROWS, LENGTH);
		network = factory.buildRoadNetwork();

		assertEquals(COLUMNS * ROWS * 4 - (ROWS * 2) - (COLUMNS * 2), network.vertexSet().size());
	}

	@Test
	public void testSegmentConnectionsNoUTurns() {
		factory = new SimpleGridRoadNetworkFactory(COLUMNS, ROWS, LENGTH);
		network = factory.buildRoadNetwork();

		int singleConnections = 0;
		int doubleConnections = 0;
		int tripleConnections = 0;

		for (Segment segment : network.vertexSet()) {
			Set<SegmentConnection> connections = network.outgoingEdgesOf(segment);
			assertTrue(connections.size() > 0); // no loose ends
			assertTrue(connections.size() < 4); // no U-turns

			switch (connections.size()) {
			case 1:
				singleConnections++;
				break;
			case 2:
				doubleConnections++;
				break;
			case 3:
				tripleConnections++;
				break;
			}

			for (SegmentConnection connection : connections) {
				assertNotEquals(segment.getSource(), network.getEdgeTarget(connection).getTarget());
			}
		}

		// just trust that these formulas are correct
		// it took lots of hand-drawn graphs to figure them out
		assertEquals(4 * 2, singleConnections);
		assertEquals((ROWS - 3) * 4 + (COLUMNS - 3) * 4 + 8 + (ROWS - 2) * 2 + (COLUMNS - 2) * 2, doubleConnections);
		assertEquals(
				(ROWS - 2) * 2 + (COLUMNS - 2) * 2 + (ROWS - 2) * (COLUMNS - 3) * 2 + (COLUMNS - 2) * (ROWS - 3) * 2,
				tripleConnections);
	}

}
