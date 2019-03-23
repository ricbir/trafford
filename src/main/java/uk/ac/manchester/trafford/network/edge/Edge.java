package uk.ac.manchester.trafford.network.edge;

import java.util.Queue;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.EvictingQueue;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

@SuppressWarnings("serial")
public class Edge extends DefaultWeightedEdge {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	public static final int JOURNEY_TIMES = 20;

	private final double length;

	protected double speedLimit;
	protected EdgeAccessController accessController;
	protected TimedTrafficLight mTrafficLight = null;

	private Agent lastAgent = null;

	private Queue<Double> lastJourneyTimes = EvictingQueue.create(JOURNEY_TIMES);

	public static EdgeBuilder build(Point from, Point to) {
		return new EdgeBuilder(from, to);
	}

	Edge(double length) {
		this.length = length;
	}

	public void setLastAgent(Agent agent) {
		this.lastAgent = agent;
	}

	public void setLastJourneyTime(double seconds) {
		lastJourneyTimes.add(seconds);
	}

	public double getAverageJourneyTime() {
		double totalJourneyTime = 0;
		for (double time : lastJourneyTimes) {
			totalJourneyTime += time;
		}
		return totalJourneyTime / lastJourneyTimes.size();
	}

	/**
	 * Calculate the congestion coefficient for this edge. A value of 1 corresponds
	 * to a complete standstill, a value of 0 to no traffic at all.
	 * 
	 * @return
	 */
	public double getCongestionCoefficient() {
		double optimalJourneyTime = length / speedLimit;
		return Math.atan((getAverageJourneyTime() - optimalJourneyTime) / optimalJourneyTime / 3) / Math.PI * 2;
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

	public void setSpeedLimith(double value) {
		this.speedLimit = value;
	}

	public Agent getLastAgent() {
		return lastAgent;
	}

	@Override
	public String toString() {
		return "{ source: " + getSource() + ", target: " + getTarget() + ", length: " + length + ", s/l: " + speedLimit
				+ " }";
	}

	public TimedTrafficLight getTrafficLight() {
		return mTrafficLight;
	}
}
