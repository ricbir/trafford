package uk.ac.manchester.trafford.network;

import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

public class GridNetworkHeuristic implements AStarAdmissibleHeuristic<Vertex> {

	private static GridNetworkHeuristic instance;

	private GridNetworkHeuristic() {

	}

	public static GridNetworkHeuristic getInstance() {
		if (instance == null) {
			instance = new GridNetworkHeuristic();
		}
		return instance;
	}

	@Override
	public double getCostEstimate(Vertex sourceVertex, Vertex targetVertex) {
		return Math.abs(sourceVertex.getX() - targetVertex.getX())
				+ Math.abs(sourceVertex.getY() - targetVertex.getY());
	}

}
