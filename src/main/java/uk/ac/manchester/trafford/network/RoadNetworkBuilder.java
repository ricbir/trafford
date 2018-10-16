package uk.ac.manchester.trafford.network;

import org.jgrapht.graph.builder.GraphBuilder;

import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgeBuilder;
import uk.ac.manchester.trafford.util.Convert;

public class RoadNetworkBuilder extends GraphBuilder<Point, Edge, RoadNetwork> {

	private RoadNetworkBuilder() {
		super(new RoadNetwork());
	}

	public static RoadNetworkBuilder RoadNetwork() {
		return new RoadNetworkBuilder();
	}

	public EdgeBuilder edge() {
		return new EdgeBuilder(this);
	}

	public RoadNetworkBuilder grid(int columns, int rows, double length, double speedLimit) {

		for (int x = 0; x < columns * length; x += length) {
			for (int y = 0; y < rows * length; y += length) {
				if (y > 0) {
					edge() //
							.from(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y)) //
							.to(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y - length)) //
							.build(); //
					edge() //
							.from(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y - length)) //
							.to(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y)) //
							.build(); //
				}
				if (x > 0) {
					edge() //
							.from(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y)) //
							.to(Convert.metersToCentimeters(x - length), Convert.metersToCentimeters(y)) //
							.build(); //
					edge() //
							.from(Convert.metersToCentimeters(x - length), Convert.metersToCentimeters(y)) //
							.to(Convert.metersToCentimeters(x), Convert.metersToCentimeters(y)) //
							.build(); //
				}
			}
		}

		return this;
	}

}
