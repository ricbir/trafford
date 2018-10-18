package uk.ac.manchester.trafford;

import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetworkBuilder;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getPackage().getName());

	private static RoadNetwork network = RoadNetworkBuilder.RoadNetwork().grid(6, 6, 20, 50).build();

	public static void main(String[] args) throws InterruptedException, PathNotFoundException, NodeNotFoundException {
		LOGGER.setLevel(Level.ALL);

		SwingRenderer renderer = new SwingRenderer();
		SimulationController controller = new SimulationController(renderer, network);

		renderer.setVisible(true);

		controller.run();

		Edge[] edges = new Edge[0];
		edges = network.edgeSet().toArray(edges);

		while (true) {
			if (network.agentSetSnapshot().length < 200) {
				int startEdgeIndex = (int) (Math.random() * edges.length);
				int destinationEdgeIndex;
				do {
					destinationEdgeIndex = (int) (Math.random() * edges.length);
				} while (destinationEdgeIndex == startEdgeIndex);

				controller.addAgent(new EdgePosition(edges[startEdgeIndex], edges[startEdgeIndex].getLength() / 2),
						new EdgePosition(edges[destinationEdgeIndex], edges[destinationEdgeIndex].getLength() / 2),
						Math.random() * 20 + 10);
			}
			Thread.sleep(50);
		}
	}
}
