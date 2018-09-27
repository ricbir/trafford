package uk.ac.manchester.trafford.network;

import org.jgrapht.graph.DefaultDirectedGraph;

public class CustomRoadNetwork extends RoadNetwork {

	/**
	 * 
	 * @param graph Graph for the network.
	 */
	public CustomRoadNetwork(DefaultDirectedGraph<Node, Edge> graph) {
		super(Type.Custom, graph);
	}

}
