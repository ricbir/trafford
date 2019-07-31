package uk.ac.manchester.trafford.network.edge;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DefaultAttribute;

import com.google.common.collect.EvictingQueue;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Vertex;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

@SuppressWarnings("serial")
public class Edge extends DefaultWeightedEdge {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	protected static final int JOURNEY_TIMES = 20;
	protected static final double DEFAULT_SPEED_LIMIT = 30;
	private static final double CONGESTION_CORRECTION_COEFFICIENT = 15;

	protected double speedLimit = DEFAULT_SPEED_LIMIT;
	protected EdgeAccessController accessController = new FreeFlowAccessController();
	protected TimedTrafficLight mTrafficLight = null;

	private Agent lastAgent = null;
	private final double length;

	private Queue<Double> lastJourneyTimes = EvictingQueue.create(JOURNEY_TIMES);

	public Edge(double length) {
		this.length = length;
		lastJourneyTimes.add(length / speedLimit);
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
		double optimalJourneyTime = getLength() / speedLimit;
		double coeff = Math.atan((getAverageJourneyTime() - optimalJourneyTime) / CONGESTION_CORRECTION_COEFFICIENT)
				/ Math.PI * 2;

		// set a cutoff to avoid negative floating point errors
		return coeff > 0.0001 ? coeff : 0.0001;
	}

	public State getAccessState() {
		return accessController.getState();
	}

	public void setAccessController(EdgeAccessController accessController) {
		this.accessController = accessController;
	}

	public EdgeAccessController getAccessController() {
		return accessController;
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

	public void setSpeedLimit(double value) {
		this.speedLimit = value;
	}

	public Agent getLastAgent() {
		return lastAgent;
	}

	public void setTrafficLight(TimedTrafficLight mTrafficLight) {
		this.mTrafficLight = mTrafficLight;
	}

	public TimedTrafficLight getTrafficLight() {
		return mTrafficLight;
	}

	@Override
	protected Vertex getSource() {
		return (Vertex) super.getSource();
	}

	@Override
	protected Vertex getTarget() {
		return (Vertex) super.getTarget();
	}

	public static ComponentAttributeProvider<Edge> attributeProvider() {
		return e -> {
			Map<String, Attribute> attributes = new HashMap<>();
			attributes.put("speedLimit", new DefaultAttribute<>(e.speedLimit, AttributeType.DOUBLE));
			attributes.put("lanes", new DefaultAttribute<>(1, AttributeType.INT));
			return attributes;
		};
	}

	public static ComponentNameProvider<Edge> idProvider() {
		return e -> {
			return e.getSource().getId() + "->" + e.getTarget().getId();
		};
	}
}
