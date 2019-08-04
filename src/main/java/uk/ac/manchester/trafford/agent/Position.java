package uk.ac.manchester.trafford.agent;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.Segment;

public class Position implements Comparable<Position> {
	private static final Random random = new Random();

	private Segment segment;
	private double distance;

	public static Position create(Segment segment, double distance) throws DistanceOutOfBoundsException {
		return new Position(segment, distance);
	}

	public static Position random(Segment origin) {
		try {
			return create(origin, random.nextDouble() * origin.getLength());
		} catch (DistanceOutOfBoundsException e) {
			// exception not possible
			return null;
		}

	}

	private Position(Segment segment, double distance) throws DistanceOutOfBoundsException {
		super();
		if (distance < 0 || distance > segment.getLength()) {
			throw new DistanceOutOfBoundsException();
		}
		this.segment = segment;
		this.distance = distance;
	}

	Position add(double delta, List<Segment> segments) throws DistanceOutOfBoundsException {
		if (!segments.contains(this.segment)) {
			throw new IllegalArgumentException();
		}

		Iterator<Segment> segmentIterator = getIteratorFromSegment(segments);
		segmentIterator.next();
		double newDistance = distance + delta;
		Segment newSegment = segment;

		while (true) {
			try {
				return Position.create(newSegment, newDistance);
			} catch (DistanceOutOfBoundsException e1) {
				if (!segmentIterator.hasNext()) {
					break;
				}
				newDistance -= newSegment.getLength();
				newSegment = segmentIterator.next();
			}
		}

		throw new DistanceOutOfBoundsException();
	}

	double distanceTo(Position position, List<Segment> segments) {
		if (!segments.contains(this.segment)) {
			throw new IllegalArgumentException();
		}
		if (!segments.contains(position.segment)) {
			throw new IllegalArgumentException();
		}

		Iterator<Segment> iterator = getIteratorFromSegment(segments);
		Segment currentSegment;
		double cumulativeDistance = 0;
		while (!(currentSegment = iterator.next()).equals(position.segment)) {
			cumulativeDistance += currentSegment.getLength();
		}

		return cumulativeDistance + position.distance - distance;
	}

	private Iterator<Segment> getIteratorFromSegment(List<Segment> segments) {
		Iterator<Segment> segmentIterator = segments.listIterator(segments.indexOf(segment));
		return segmentIterator;
	}

	protected Segment getSegment() {
		return segment;
	}

	protected double getDistance() {
		return distance;
	}

	public double getX() {
		return segment.getPointX(distance);
	}

	public double getY() {
		return segment.getPointY(distance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(distance, segment);
	}

	@Override
	public int compareTo(Position other) {
		if (!other.segment.equals(segment)) {
			throw new IllegalArgumentException("Cannot compare two positions with different segments");
		}

		if (distance > other.distance) {
			return 1;
		} else if (other.distance > distance) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		return Double.doubleToLongBits(distance) == Double.doubleToLongBits(other.distance)
				&& Objects.equals(segment, other.segment);
	}

}
