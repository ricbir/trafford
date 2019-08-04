package uk.ac.manchester.trafford.agent;

import java.util.List;

import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;

public class LeaderFinder {

	private final RoadNetwork network;

	public LeaderFinder(RoadNetwork network) {
		this.network = network;

	}

	public Agent findLeader(List<Segment> segments, double rangeStart, double rangeEnd) {
		double minDistance = rangeStart;
		double maxDistance = rangeEnd;

		for (Segment segment : segments) {
			List<Agent> agents = network.getAgentsOnSegment(segment);
			for (Agent agent : agents) {
				if (agent.getPosition().getDistance() > minDistance
						&& agent.getPosition().getDistance() < maxDistance) {
					return agent;
				}
			}
			minDistance -= segment.getLength();
			maxDistance -= segment.getLength();
			if (maxDistance <= 0) {
				return null;
			}
		}
		return null;
	}

}
