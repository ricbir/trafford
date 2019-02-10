package uk.ac.manchester.trafford.network.edge;

public class EdgePosition {
	// private final Graph<Point, Edge> graph;
	private final Edge edge;
	private final double distance;

	public EdgePosition(Edge edge, double distance) {
		// this.graph = graph;
		this.edge = edge;
		this.distance = distance;
	}

	/*
	 * public Graph<Point, Edge> getGraph() { return graph; }
	 */

	public Edge getEdge() {
		return edge;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}

		EdgePosition pos = (EdgePosition) o;
		return pos.edge == edge && pos.distance == distance;
	}

	@Override
	public int hashCode() {
		int result = edge.hashCode();
		result = 31 * result + Double.hashCode(distance);
		return result;
	}

	@Override
	public String toString() {
		return "d: " + distance + ", e: " + edge;
	}
}
