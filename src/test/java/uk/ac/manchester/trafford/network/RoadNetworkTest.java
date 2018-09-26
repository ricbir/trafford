package uk.ac.manchester.trafford.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.RoadNetwork.Type;

public class RoadNetworkTest {

	private RoadNetwork network;
	private Graph<Node, Edge> graph;

	@Before
	public void setUp() throws Exception {
		graph = new DefaultDirectedWeightedGraph<>(Edge.class);
		network = new TestNetwork(Type.Custom, graph);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShortestPath() throws NodeNotFoundException {
		network.addNode("source");
		network.addNode("a0");
		network.addNode("a1");
		network.addNode("a2");
		network.addNode("a3");
		network.addNode("a4");
		network.addNode("b0");
		network.addNode("b1");
		network.addNode("b2");
		network.addNode("b3");
		network.addNode("b4");
		network.addNode("target");

		network.addEdge("source", "a0", 100, 30);
		network.addEdge("a0", "a1", 100, 30);
		network.addEdge("a1", "a2", 100, 30);
		network.addEdge("a2", "a3", 100, 30);
		network.addEdge("a3", "a4", 100, 30);
		network.addEdge("a4", "target", 100, 30);
		network.addEdge("source", "b0", 10, 30);
		network.addEdge("b0", "b1", 10, 50);
		network.addEdge("b1", "b2", 10, 50);
		network.addEdge("b2", "b3", 10, 50);
		network.addEdge("b3", "b4", 10, 50);
		network.addEdge("b4", "target", 10, 50);

		GraphPath<Node, Edge> path = null;
		try {
			path = network.findPath("source", "target");
		} catch (PathNotFoundException e) {
			fail(e.getMessage());
		}

		// TODO test this in a better way
		assertEquals("[(source : b0), (b0 : b1), (b1 : b2), (b2 : b3), (b3 : b4), (b4 : target)]",
				path.getEdgeList().toString());

	}

	@Test
	public void testFastestPath() throws NodeNotFoundException {
		network.addNode("source");
		network.addNode("a0");
		network.addNode("a1");
		network.addNode("a2");
		network.addNode("a3");
		network.addNode("a4");
		network.addNode("b0");
		network.addNode("b1");
		network.addNode("b2");
		network.addNode("b3");
		network.addNode("b4");
		network.addNode("target");

		network.addEdge("source", "a0", 100, 90);
		network.addEdge("a0", "a1", 100, 90);
		network.addEdge("a1", "a2", 100, 90);
		network.addEdge("a2", "a3", 100, 90);
		network.addEdge("a3", "a4", 100, 90);
		network.addEdge("a4", "target", 100, 90);
		network.addEdge("source", "b0", 80, 50);
		network.addEdge("b0", "b1", 80, 50);
		network.addEdge("b1", "b2", 80, 50);
		network.addEdge("b2", "b3", 80, 50);
		network.addEdge("b3", "b4", 80, 50);
		network.addEdge("b4", "target", 80, 50);

		GraphPath<Node, Edge> path = null;
		try {
			path = network.findPath("source", "target");
		} catch (PathNotFoundException e) {
			fail(e.getMessage());
		}

		// TODO test this in a better way
		assertEquals("[(source : a0), (a0 : a1), (a1 : a2), (a2 : a3), (a3 : a4), (a4 : target)]",
				path.getEdgeList().toString());

	}

	private class TestNetwork extends RoadNetwork {

		TestNetwork(Type type, Graph<Node, Edge> graph) {
			super(type, graph);
		}

	}

}
