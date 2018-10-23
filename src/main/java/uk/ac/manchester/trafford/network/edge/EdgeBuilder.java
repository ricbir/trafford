package uk.ac.manchester.trafford.network.edge;

import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;

public class EdgeBuilder {

	private Point from;
	private Point to;
	private double speedLimit = 200;
	private EdgeAccessController accessController = new FreeFlowAccessController();

	EdgeBuilder(Point from, Point to) {
		this.from = from;
		this.to = to;
	}

	public EdgeBuilder accessController(EdgeAccessController accessController) {
		this.accessController = accessController;
		return this;
	}

	public EdgeBuilder speedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
		return this;
	}

	public Edge addToNetwork(RoadNetwork network) {
		Edge edge = new Edge(from.distance(to));
		edge.speedLimit = speedLimit;
		edge.accessController = accessController;
		network.addVertex(from);
		network.addVertex(to);
		network.addEdge(from, to, edge);
		network.setEdgeWeight(edge, edge.getLength() / edge.getSpeedLimit());
		return edge;
	}
}
