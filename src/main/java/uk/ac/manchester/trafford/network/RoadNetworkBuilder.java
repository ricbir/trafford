package uk.ac.manchester.trafford.network;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;
import uk.ac.manchester.trafford.util.Convert;

public class RoadNetworkBuilder {

	private RoadNetwork network;

	private RoadNetworkBuilder() {
		network = new RoadNetwork();
	}

	public static RoadNetworkBuilder RoadNetwork() {
		return new RoadNetworkBuilder();
	}

	private enum XingOffset {
		N_IN(-1, -2), S_IN(1, 2), W_IN(-2, 1), E_IN(2, -1), N_OUT(1, -2), S_OUT(-1, 2), W_OUT(-2, -1), E_OUT(2, 1);

		public int x;
		public int y;

		private XingOffset(int x, int y) {
			this.x = x * Convert.metersToCentimeters(Constants.LANE_WIDTH / 2);
			this.y = y * Convert.metersToCentimeters(Constants.LANE_WIDTH / 2);
		}
	}

	public RoadNetworkBuilder grid(int columns, int rows, double length, double speedLimit) {

		int lengthCentimeters = Convert.metersToCentimeters(length);

		// N_OUT N_IN
		// | |
		// W_OUT - - E_IN
		//
		// W_IN - - E_OUT
		// | |
		// S_OUT S_IN

		for (int x = 0; x < columns * lengthCentimeters; x += lengthCentimeters) {
			for (int y = 0; y < rows * lengthCentimeters; y += lengthCentimeters) {
				Point nOut = new Point(x + XingOffset.N_OUT.x, y + XingOffset.N_OUT.y);
				Point nIn = new Point(x + XingOffset.N_IN.x, y + XingOffset.N_IN.y);
				Point sOut = new Point(x + XingOffset.S_OUT.x, y + XingOffset.S_OUT.y);
				Point sIn = new Point(x + XingOffset.S_IN.x, y + XingOffset.S_IN.y);
				Point eOut = new Point(x + XingOffset.E_OUT.x, y + XingOffset.E_OUT.y);
				Point eIn = new Point(x + XingOffset.E_IN.x, y + XingOffset.E_IN.y);
				Point wOut = new Point(x + XingOffset.W_OUT.x, y + XingOffset.W_OUT.y);
				Point wIn = new Point(x + XingOffset.W_IN.x, y + XingOffset.W_IN.y);

				TimedTrafficLight trafficLight = new TimedTrafficLight(10, 3, 2, network);

				if (y > 0) {
					Edge.build(nOut, new Point(x + XingOffset.S_IN.x, y - lengthCentimeters + XingOffset.S_IN.y))
							.speedLimit(speedLimit).addToNetwork(network);
					Edge.build(new Point(x + XingOffset.S_OUT.x, y - lengthCentimeters + XingOffset.S_OUT.y), nIn)
							.speedLimit(speedLimit).addToNetwork(network);

					if (y < (rows - 1) * lengthCentimeters) {
						Edge.build(nIn, sOut).accessController(trafficLight.getController(0)).speedLimit(speedLimit)
								.addToNetwork(network);
					}

					if (x < (columns - 1) * lengthCentimeters) {
						Edge.build(nIn, eOut).accessController(trafficLight.getController(0)).speedLimit(3)
								.addToNetwork(network);
					}

					if (x > 0) {
						Edge.build(nIn, wOut).accessController(trafficLight.getController(0)).speedLimit(3)
								.addToNetwork(network);
					}
				}
				if (x > 0) {
					Edge.build(wOut, new Point(x - lengthCentimeters + XingOffset.E_IN.x, y + XingOffset.E_IN.y)) //
							.speedLimit(speedLimit).addToNetwork(network); //
					Edge.build(new Point(x - lengthCentimeters + XingOffset.E_OUT.x, y + XingOffset.E_OUT.y), wIn) //
							.speedLimit(speedLimit).addToNetwork(network); //

					if (x < (columns - 1) * lengthCentimeters) {
						Edge.build(wIn, eOut).accessController(trafficLight.getController(1)).speedLimit(speedLimit)
								.addToNetwork(network);
					}

					if (y < (rows - 1) * lengthCentimeters) {
						Edge.build(wIn, sOut).accessController(trafficLight.getController(1)).speedLimit(3)
								.addToNetwork(network);
					}

					if (y > 0) {
						Edge.build(wIn, nOut).accessController(trafficLight.getController(1)).speedLimit(3)
								.addToNetwork(network);
					}
				}

				if (x < (columns - 1) * lengthCentimeters) {
					if (y < (rows - 1) * lengthCentimeters) {
						Edge.build(eIn, sOut).accessController(trafficLight.getController(1)).speedLimit(3)
								.addToNetwork(network);
					}

					if (y > 0) {
						Edge.build(eIn, nOut).accessController(trafficLight.getController(1)).speedLimit(3)
								.addToNetwork(network);
					}

					if (x > 0) {
						Edge.build(eIn, wOut).accessController(trafficLight.getController(1)).speedLimit(speedLimit)
								.addToNetwork(network);
					}
				}

				if (y < (rows - 1) * lengthCentimeters) {
					if (x < (columns - 1) * lengthCentimeters) {
						Edge.build(sIn, eOut).accessController(trafficLight.getController(0)).speedLimit(3)
								.addToNetwork(network);
					}

					if (y > 0) {
						Edge.build(sIn, nOut).accessController(trafficLight.getController(0)).speedLimit(speedLimit)
								.addToNetwork(network);
					}

					if (x > 0) {
						Edge.build(sIn, wOut).accessController(trafficLight.getController(0)).speedLimit(3)
								.addToNetwork(network);
					}
				}

			}
		}

		return this;
	}

	public RoadNetwork build() {
		return network;
	}

}
