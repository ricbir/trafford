package uk.ac.manchester.trafford.agent;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;
import uk.ac.manchester.trafford.network.SegmentConnection;

public class Agent {

	private Position position;
	private Position destination;
	private double speed = 0;
	private double maxSpeed = 30;

	private ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm;
	private GraphPath<Segment, SegmentConnection> path;

	private IDMAccelerator accelerator;

	public Agent(RoadNetwork network, Position startPosition,
			Position destination, ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm, IDMAccelerator accelerator) {
		this.routingAlgorithm = routingAlgorithm;
		this.position = startPosition;
		this.destination = destination;
		this.accelerator = accelerator;
		updatePath();
	}

	public void update() {
		if (!hasArrived()) {
			updateSpeed();

			double delta = speed / Constants.UPDATES_PER_SECOND;
			List<Segment> segments = path.getVertexList();
			Segment oldSegment = position.getSegment();
			try {
				int currentSegmentIndex = segments.indexOf(position.getSegment()) + 1;
				position = position.add(delta, segments.subList(currentSegmentIndex, segments.size()));
			} catch (DistanceOutOfBoundsException e) {
				throw new IllegalArgumentException(e);
			}

			if (!position.getSegment().equals(oldSegment)) {
				updatePath();
			}
		}
	}

	private void updateSpeed() {
		if (position.getSegment().equals(destination.getSegment())) {
			speed += accelerator.getAcceleration(speed, maxSpeed,
					destination.getDistance() - position.getDistance() + Constants.MINIMUM_SPACING, 0)
					/ Constants.UPDATES_PER_SECOND;
		} else {
			speed += accelerator.getAcceleration(speed, maxSpeed) / Constants.UPDATES_PER_SECOND;
		}

		if (speed < 0.01) {
			speed = 0;
		}
	}

	private void updatePath() {
		this.path = routingAlgorithm.getPath(position.getSegment(), destination.getSegment());
	}

	public Position getPosition() {
		return position;
	}

	public double getX() {
		return position.getX();

	}

	public double getY() {
		return position.getY();
	}

	public boolean hasArrived() {
		if (!position.getSegment().equals(destination.getSegment())) {
			return false;
		}

		return position.compareTo(destination) >= 0;
	}

	public Segment getSegment() {
		return position.getSegment();
	}

	public void setRoutingAlgorithm(ShortestPathAlgorithm<Segment, SegmentConnection> algo) {
		this.routingAlgorithm = algo;
	}
}
