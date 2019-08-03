package uk.ac.manchester.trafford.network;

import java.util.SortedSet;

import org.jgrapht.graph.DefaultDirectedGraph;

import uk.ac.manchester.trafford.agent.Agent;

public class RoadNetwork extends DefaultDirectedGraph<Segment, SegmentConnection> {

	public RoadNetwork() {
		super(SegmentConnection.class);
		// TODO Auto-generated constructor stub
	}

	public void update() {
		for (Segment segment : vertexSet()) {
			SortedSet<Agent> agentsOnSegment = segment.getAgents();
			for (Agent agent : agentsOnSegment) {
				if (agent.getSegment() != segment) {
					agentsOnSegment.remove(agent);
					agent.getSegment().getAgents().add(agent);
				}
			}
		}
	}
}
