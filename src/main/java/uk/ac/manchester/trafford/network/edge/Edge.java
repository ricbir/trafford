package uk.ac.manchester.trafford.network.edge;

import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

@SuppressWarnings("serial")
public class Edge extends DefaultWeightedEdge {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	private final double length;

	protected double speedLimit;
	protected EdgeAccessController accessController;

	private Agent lastAgent = null;

	public static EdgeBuilder build(Point from, Point to) {
		return new EdgeBuilder(from, to);
	}

	Edge(double length) {
		this.length = length;
	}

	public State getAccessState() {
		return accessController.getState();
	}

	/**
	 * Returns the length of the edge in meters
	 * 
	 * @return1
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the speed limit of the edge in m/s
	 * 
	 * @return
	 */
	public double getSpeedLimit() {
		return speedLimit;
	}

	public Agent getLastAgent() {
		return lastAgent;
	}

	@Override
	public String toString() {
		return "{ source: " + getSource() + ", target: " + getTarget() + ", length: " + length + ", s/l: " + speedLimit
				+ " }";
	}

	public void setLastAgent(Agent agent) {
		this.lastAgent = agent;
	}
}
