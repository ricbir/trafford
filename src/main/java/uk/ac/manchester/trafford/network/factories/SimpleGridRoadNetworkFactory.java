package uk.ac.manchester.trafford.network.factories;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;

public class SimpleGridRoadNetworkFactory implements RoadNetworkFactory {

	private int columns;
	private int rows;
	private double length;
	private boolean uTurnsAllowed = false;

	public SimpleGridRoadNetworkFactory(int columns, int rows, double length) {
		this.columns = columns;
		this.rows = rows;
		this.length = length;
	}

	@Override
	public RoadNetwork buildRoadNetwork() {
		Table<Point, Point, Segment> segments = HashBasedTable.create();
		RoadNetwork network = new RoadNetwork();

		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < rows; j++) {
				Point a = Point.create(i * length, j * length);

				Segment segment;
				if (i > 0) {
					Point b = Point.create((i - 1) * length, j * length);

					segment = new Segment(a, b);
					segments.put(a, b, segment);
					network.addVertex(segment);

					segment = new Segment(b, a);
					segments.put(b, a, segment);
					network.addVertex(segment);
				}

				if (j > 0) {
					Point c = Point.create(i * length, (j - 1) * length);
					segment = new Segment(a, c);
					segments.put(a, c, segment);
					network.addVertex(segment);

					segment = new Segment(c, a);
					segments.put(c, a, segment);
					network.addVertex(segment);
				}
			}
		}

		for (Segment segment : segments.values()) {
			network.addVertex(segment);

			for (Segment outSegment : segments.row(segment.getTarget()).values()) {
				// connect all outgoing segments except the one that goes back to the source (no
				// U turns)
				if (uTurnsAllowed || !outSegment.getTarget().equals(segment.getSource())) {
					network.addEdge(segment, outSegment);
				}
			}
		}

		return network;
	}

}
