package uk.ac.manchester.trafford.network.edge;

import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetworkBuilder;
import uk.ac.manchester.trafford.util.Convert;

public class EdgeBuilder {
	private double speedLimit = 200;

	private Point from;
	private Point to;

	private RoadNetworkBuilder roadNetworkBuilder;

	public EdgeBuilder(RoadNetworkBuilder roadNetworkBuilder) {
		this.roadNetworkBuilder = roadNetworkBuilder;
	}

	public EdgeBuilder from(int x, int y) {
		from = new Point(x, y);
		return this;
	}

	public EdgeBuilder to(int x, int y) {
		to = new Point(x, y);
		return this;
	}

	public EdgeBuilder speedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
		return this;
	}

	public RoadNetworkBuilder build() {
		Edge edge = new Edge(Convert.centimetersToMeters(from.distance(to)));
		edge.setSpeedLimit(speedLimit);
		roadNetworkBuilder.addVertices(from, to);
		roadNetworkBuilder.addEdge(from, to, edge, edge.getLength() / edge.getSpeedLimit());
		return roadNetworkBuilder;
	}
}