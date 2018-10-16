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

	public EdgeBuilder from(Point point) {
		from = point;
		return this;
	}

	public EdgeBuilder to(Point point) {
		to = point;
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