package uk.ac.manchester.trafford.network.edge;

import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Vertex;

public class EdgeBuilder {

	private Vertex from;
	private Vertex to;
	private double speedLimit = 200;
	private EdgeAccessController accessController = new FreeFlowAccessController();
	private TimedTrafficLight mTrafficLight = null;

	EdgeBuilder(Vertex from, Vertex to) {
		this.from = from;
		this.to = to;
	}

	public EdgeBuilder accessController(EdgeAccessController accessController) {
		this.accessController = accessController;
		return this;
	}

	public EdgeBuilder trafficLight(TimedTrafficLight trafficLight) {
		this.mTrafficLight = trafficLight;
		return this;
	}

	public EdgeBuilder speedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
		return this;
	}

	public Edge addToNetwork(RoadNetwork network) {
		Edge edge = build();
		network.addVertex(from);
		network.addVertex(to);
		network.addEdge(from, to, edge);
		network.setEdgeWeight(edge, from.distance(to) / edge.speedLimit);
		return edge;
	}

	public Edge build() {
		Edge edge = new Edge();
		edge.speedLimit = speedLimit;
		edge.accessController = accessController;
		edge.mTrafficLight = this.mTrafficLight;
		for (int i = 0; i < Edge.JOURNEY_TIMES; i++) {
			edge.setLastJourneyTime(from.distance(to) / edge.speedLimit);
		}
		return edge;
	}
}
