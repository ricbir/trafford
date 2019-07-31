package uk.ac.manchester.trafford.agent;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import uk.ac.manchester.trafford.network.edge.Edge;

public class Position {

	private static final Logger LOGGER = LogManager.getLogger(Position.class);

	private Edge edge;
	private double distance;
	private final SimpleDoubleProperty xProperty;
	private final SimpleDoubleProperty yProperty;

	private double edgeSin;
	private double edgeCos;

	public Position(Edge edge, double distance) {
		this.edge = edge;
		this.distance = distance;
		this.xProperty = new SimpleDoubleProperty();
		this.yProperty = new SimpleDoubleProperty();
	}

	public Edge getEdge() {
		return edge;
	}

	protected void setEdge(Edge edge) {
		this.edge = edge;
		if (edge != null) {
			updateCoordinates();
		}
	}

	public double getDistance() {
		return distance;
	}

	protected void setDistance(double distance) {
		this.distance = distance;
		updateCoordinates();
	}

	private void updateCoordinates() {
		xProperty.set(edge.getX(distance));
		yProperty.set(edge.getY(distance));
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

		Position pos = (Position) o;
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

	public ObservableValue<? extends Number> getXProperty() {
		return xProperty;
	}

	public ObservableValue<? extends Number> getYProperty() {
		return yProperty;
	}
}
