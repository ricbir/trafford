package uk.ac.manchester.trafford.network;

/**
 * Just a very basic point class with double precision
 * 
 * @author ricbir
 *
 */
public class Point {
	private final double x;
	private final double y;

	public static Point create(double x, double y) {
		return new Point(x, y);
	}

	private Point(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double distanceTo(Point p) {
		return Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Point other = (Point) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

}
