package uk.ac.manchester.trafford.network;

import uk.ac.manchester.trafford.util.Convert;

public class Point {

	private final int x;
	private final int y;

	/**
	 * A point on a plane.
	 * 
	 * @param x Distance from origin on X axis, in cm.
	 * @param y Distance from origin on Y axis, in cm.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
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
		Point node = (Point) o;

		return node.x == x && node.y == y;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * Return the distance between this point and the target, in meters.
	 * 
	 * @param target
	 * @return The distance in meters.
	 */
	public double distance(Point target) {
		return Convert
				.centimetersToMeters(Math.sqrt((x - target.x) * (x - target.x) + (y - target.y) * (y - target.y)));
	}
}
