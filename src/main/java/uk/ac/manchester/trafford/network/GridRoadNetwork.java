package uk.ac.manchester.trafford.network;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

public class GridRoadNetwork extends RoadNetwork {
	public static final int DEFAULT_EDGE_LENGTH = 100000;

	/**
	 * Create a grid shaped road network.
	 * 
	 * @param height     Height of the grid, in number of nodes.
	 * @param width      Width of the grid, in number of nodes.
	 * @param edgeLength Lenght of edges, in meters.
	 * @param speedLimit Speed limit, in km/h.
	 */
	public GridRoadNetwork(int height, int width, double edgeLength, double speedLimit) {
		super(Type.Grid, new DefaultDirectedWeightedGraph<>(Edge.class));

		// Create a width-by-height grid graph, with nodes connected in both directions
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				addNode(i + "." + j);
				if (j > 0) {
					addEdge(i + "." + j, i + "." + (j - 1), edgeLength, speedLimit);
					addEdge(i + "." + (j - 1), i + "." + j, edgeLength, speedLimit);
				}
				if (i > 0) {
					addEdge(i + "." + j, (i - 1) + "." + j, edgeLength, speedLimit);
					addEdge((i - 1) + "." + j, i + "." + j, edgeLength, speedLimit);
				}
			}

		}
	}
}
