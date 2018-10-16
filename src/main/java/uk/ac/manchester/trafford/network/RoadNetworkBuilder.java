package uk.ac.manchester.trafford.network;

import org.jgrapht.graph.builder.GraphBuilder;

import uk.ac.manchester.trafford.Constants;
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

				if (y > 0) {
					edge() //
							.from(nOut) //
							.to(new Point(x + XingOffset.S_IN.x, y - lengthCentimeters + XingOffset.S_IN.y)) //
							.build(); //
					edge() //
							.from(new Point(x + XingOffset.S_OUT.x, y - lengthCentimeters + XingOffset.S_OUT.y)) //
							.to(nIn) //
							.build(); //

					if (y < (rows - 1) * lengthCentimeters) {
						edge().from(nIn).to(sOut).build();
					}

					if (x < (columns - 1) * lengthCentimeters) {
						edge().from(nIn).to(eOut).build();
					}

					if (x > 0) {
						edge().from(nIn).to(wOut).build();
					}
				}
				if (x > 0) {
					edge() //
							.from(wOut) //
							.to(new Point(x - lengthCentimeters + XingOffset.E_IN.x, y + XingOffset.E_IN.y)) //
							.build(); //
					edge() //
							.from(new Point(x - lengthCentimeters + XingOffset.E_OUT.x, y + XingOffset.E_OUT.y)) //
							.to(wIn) //
							.build(); //

					if (x < (columns - 1) * lengthCentimeters) {
						edge().from(wIn).to(eOut).build();
					}

					if (y < (rows - 1) * lengthCentimeters) {
						edge().from(wIn).to(sOut).build();
					}

					if (y > 0) {
						edge().from(wIn).to(nOut).build();
					}
				}

				if (x < (columns - 1) * lengthCentimeters) {
					if (y < (rows - 1) * lengthCentimeters) {
						edge().from(eIn).to(sOut).build();
					}

					if (y > 0) {
						edge().from(eIn).to(nOut).build();
					}

					if (x > 0) {
						edge().from(eIn).to(wOut).build();
					}
				}

				if (y < (rows - 1) * lengthCentimeters) {
					if (x < (columns - 1) * lengthCentimeters) {
						edge().from(sIn).to(eOut).build();
					}

					if (y > 0) {
						edge().from(sIn).to(nOut).build();
					}

					if (x > 0) {
						edge().from(sIn).to(wOut).build();
					}
				}

			}
		}

		return this;
	}

}
