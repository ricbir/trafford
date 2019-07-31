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
import org.jgrapht.io.EdgeProvider;

import com.google.common.collect.EvictingQueue;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Vertex;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

@SuppressWarnings("serial")
public class Edge extends DefaultWeightedEdge {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Edge.class.getName());

	public static final int JOURNEY_TIMES = 20;

	protected double speedLimit;
	protected EdgeAccessController accessController;
	protected TimedTrafficLight mTrafficLight = null;

	private Agent lastAgent = null;

	private Queue<Double> lastJourneyTimes = EvictingQueue.create(JOURNEY_TIMES);

	public static EdgeBuilder build(Vertex from, Vertex to) {
		return new EdgeBuilder(from, to);
	}

	Edge() {
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
		return Math.atan((getAverageJourneyTime() - optimalJourneyTime) / optimalJourneyTime / 3) / Math.PI * 2;
	}

	public State getAccessState() {
		return accessController.getState();
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
		return getSource().distance(getTarget());
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

	public static EdgeProvider<Vertex, Edge> provider() {
		return (source, target, label, attributes) -> {
			EdgeBuilder builder = Edge.build(source, target);
			if (attributes.containsKey("speedLimit")) {
				builder.speedLimit(Double.parseDouble(attributes.get("speedLimit").getValue()));
			}
			return builder.build();
		};
	}

	public static ComponentNameProvider<Edge> idProvider() {
		return e -> {
			return e.getSource().getId() + "->" + e.getTarget().getId();
		};
	}
}
