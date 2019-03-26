package uk.ac.manchester.trafford.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.VertexProvider;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Vertex {
	private static final Logger LOGGER = LogManager.getLogger(Vertex.class);

	private final DoubleProperty x;
	private final DoubleProperty y;
	private final String id;

	private static int idCounter = 0;

	/**
	 * 
	 * @param x Distance from origin on X axis, in m.
	 * @param y Distance from origin on Y axis, in m.
	 */
	public Vertex(double x, double y) {
		this(Integer.toString(idCounter++), x, y);
	}

	/**
	 * 
	 * @param id A unique id for this point.
	 * @param x  Distance from origin on X axis, in m.
	 * @param y  Distance from origin on Y axis, in m.
	 */
	public Vertex(String id, double x, double y) {
		this.id = id;
		this.x = new SimpleDoubleProperty(x);
		this.y = new SimpleDoubleProperty(y);
	}

	public double getX() {
		return x.get();
	}

	public double getY() {
		return y.get();
	}

	public DoubleProperty getXProperty() {
		return x;
	}

	public DoubleProperty getYProperty() {
		return y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Vertex))
			return false;
		Vertex other = (Vertex) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public static ComponentAttributeProvider<Vertex> attributeProvider() {
		return v -> {
			Map<String, Attribute> attributes = new HashMap<>();
			attributes.put("pos", new DefaultAttribute<>(v.x.get() + "," + v.y.get() + "!", AttributeType.STRING));
			attributes.put("x", new DefaultAttribute<>(v.x.get(), AttributeType.DOUBLE));
			attributes.put("y", new DefaultAttribute<>(v.y.get(), AttributeType.DOUBLE));
			return attributes;
		};
	}

	public static ComponentNameProvider<Vertex> idProvider() {
		return v -> {
			return "\"" + v.id + "\"";
		};
	}

	public static ComponentNameProvider<Vertex> labelProvider() {
		return v -> {
			return v.id;
		};
	}

	/**
	 * Return the distance between this point and the target, in meters.
	 * 
	 * @param target
	 * @return The distance in meters.
	 */
	public double distance(Vertex target) {
		return Math.sqrt(Math.pow(getX() - target.getX(), 2) + Math.pow(getY() - target.getY(), 2));
	}

	public String getId() {
		return id;
	}

	public static VertexProvider<Vertex> provider() {
		return (id, attributes) -> {
			Vertex v = new Vertex(id, Double.parseDouble(attributes.get("x").getValue()),
					Double.parseDouble(attributes.get("y").getValue()));
			LOGGER.debug("Importing vertex " + v);
			return v;
		};
	}
}
