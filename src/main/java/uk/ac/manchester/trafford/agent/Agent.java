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
	private double speed = 30;

	private ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm;
	private GraphPath<Segment, SegmentConnection> path;

	public Agent(RoadNetwork network, ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm,
			Position startPosition, Position destination) {
		this.routingAlgorithm = routingAlgorithm;
		this.position = startPosition;
		this.destination = destination;
		updatePath();
	}

	public void update() {
		if (!hasArrived()) {
			double delta = speed / Constants.UPDATES_PER_SECOND;
			List<Segment> segments = path.getVertexList();
			Segment oldSegment = position.getSegment();
			try {
				position = position.add(delta,
						segments.subList(segments.indexOf(position.getSegment()) + 1, segments.size()));
			} catch (DistanceOutOfBoundsException e) {
				throw new IllegalArgumentException(e);
			}

			if (!position.getSegment().equals(oldSegment)) {
				updatePath();
			}
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
