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

	private final double maxSpeed = 30;

	private Position position;
	private Position destination;
	private double speed = 0;

	private ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm;
	private GraphPath<Segment, SegmentConnection> path;

	private IDMAccelerator accelerator;

	private Agent leader = null;
	private LeaderFinder leaderFinder;

	public Agent(RoadNetwork network, Position startPosition, Position destination, IDMAccelerator accelerator,
			ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm, LeaderFinder leaderFinder) {
		this.routingAlgorithm = routingAlgorithm;
		this.position = startPosition;
		this.destination = destination;
		this.accelerator = accelerator;
		this.leaderFinder = leaderFinder;
		updatePath();
	}

	public void update() {
		if (!hasArrived()) {
			updateLeader();
			updateSpeed();
			updatePosition();
		}
	}

	private void updatePosition() {
		double delta = speed / Constants.UPDATES_PER_SECOND;
		List<Segment> segments = getRemainingPath();
		try {
			position = position.add(delta, segments.subList(1, segments.size()));
		} catch (DistanceOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void updateSpeed() {
		double leaderDelta = 1000;
		double destinationDelta = 1000;
		double delta;

		double destinationDistance = position.distanceTo(destination, getRemainingPath());

		if (destinationDistance < Constants.LOOK_AHEAD_DISTANCE) {
			destinationDelta = accelerator.getAcceleration(speed, maxSpeed,
					destinationDistance + Constants.MINIMUM_SPACING, 0) / Constants.UPDATES_PER_SECOND;
		} else {
			destinationDelta = accelerator.getAcceleration(speed, maxSpeed) / Constants.UPDATES_PER_SECOND;
		}

		if (leader != null) {
			leaderDelta = accelerator.getAcceleration(speed, maxSpeed,
					position.distanceTo(leader.position, getRemainingPath()), leader.speed)
					/ Constants.UPDATES_PER_SECOND;
		}

		delta = leaderDelta < destinationDelta ? leaderDelta : destinationDelta;

		speed += delta;

		if (speed < 0.01) {
			speed = 0;
		}
	}

	private void updatePath() {
		this.path = routingAlgorithm.getPath(position.getSegment(), destination.getSegment());
	}

	private void updateLeader() {
		// TODO only update if necessary
		leader = leaderFinder.findLeader(getRemainingPath(), position.getDistance(),
				position.getDistance() + Constants.LOOK_AHEAD_DISTANCE);
	}

	private List<Segment> getRemainingPath() {
		List<Segment> segments = path.getVertexList();
		int currentSegmentIndex = segments.indexOf(position.getSegment());
		return segments.subList(currentSegmentIndex, segments.size());

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
