package uk.ac.manchester.trafford.network;

import java.util.function.Function;

import uk.ac.manchester.trafford.network.edge.Edge;

public class WeightCalculator implements Function<Edge, Double> {

	private static WeightCalculator instance;

	private WeightCalculator() {

	}

	public static WeightCalculator getInstance() {
		if (instance == null) {
			instance = new WeightCalculator();
		}
		return instance;
	}

	@Override
	public Double apply(Edge edge) {
		return edge.getLength() / edge.getSpeedLimit() + edge.getAverageJourneyTime();
	}

}
