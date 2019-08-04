package uk.ac.manchester.trafford.network;

public class Segment {
	private final Point source;
	private final Point target;
	private final double length;

	private final double cosine;
	private final double sine;

	public Segment(Point source, Point target) {
		this.source = source;
		this.target = target;
		length = source.distanceTo(target);

		this.cosine = (target.getX() - source.getX()) / length;
		this.sine = (target.getY() - source.getY()) / length;
	}

	public Point getSource() {
		return source;
	}

	public Point getTarget() {
		return target;
	}

	public double getLength() {
		return length;
	}

	public double getPointX(double distance) {
		return source.getX() + distance * cosine;
	}

	public double getPointY(double distance) {
		return source.getY() + distance * sine;
	}

	@Override
	public String toString() {
		return "{" + source + ", " + target + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

}
