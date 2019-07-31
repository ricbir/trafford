package uk.ac.manchester.trafford.network;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.GraphImporter;
import org.jgrapht.io.ImportException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;

public class RoadNetworkFactory {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(RoadNetworkFactory.class);

	private static RoadNetworkFactory factory;

	private static final double TURN_SPEED_LIMIT = 3;

	public static RoadNetworkFactory getFactory() {
		if (factory == null) {
			factory = new RoadNetworkFactory();
		}
		return factory;
	}

	private enum XingDir {
		N_IN("N_IN", -1, -2), S_IN("S_IN", 1, 2), W_IN("W_IN", -2, 1), E_IN("E_IN", 2, -1), N_OUT("N_OUT", 1, -2),
		S_OUT("S_OUT", -1, 2), W_OUT("W_OUT", -2, -1), E_OUT("E_OUT", 2, 1);

		public double x;
		public double y;
		public String label;

		private XingDir(String label, int x, int y) {
			this.label = label;
			this.x = x * Constants.LANE_WIDTH / 2;
			this.y = y * Constants.LANE_WIDTH / 2;
		}
	}

	public RoadNetwork importFromFile(File file) throws ImportException {
		RoadNetwork network = new RoadNetwork();

		GraphImporter<Vertex, Edge> exporter = new DOTImporter<>(Vertex.provider(), null, null, null);
		exporter.importGraph(network, file);

		return network;
	}

	public RoadNetwork grid(int columns, int rows, double length, double speedLimit) {
		RoadNetwork network = new RoadNetwork();

		TimedTrafficLight[][] trafficLights = new TimedTrafficLight[columns][rows];
		Table<Integer, Integer, Map<XingDir, Vertex>> intersections = HashBasedTable.create();

		int counter = 0;

		for (int column = 0; column < columns; column++) {
			for (int row = 0; row < rows; row++) {
				trafficLights[column][row] = new TimedTrafficLight(++counter, 10, 3, 2, network);

				Map<XingDir, Vertex> vertices = new HashMap<>();

				double x = column * length;
				double y = row * length;
				for (XingDir dir : XingDir.values()) {
					Vertex v = new Vertex(column + ":" + row + ":" + dir.label, x + dir.x, y + dir.y);
					network.addVertex(v);
					vertices.put(dir, v);
				}

				intersections.put(column, row, vertices);
			}
		}

		counter = 0;
		Edge edge;

		for (int column = 0; column < columns; column++) {
			for (int row = 0; row < rows; row++) {

				Vertex nOut = intersections.get(column, row).get(XingDir.N_OUT);
				Vertex nIn = intersections.get(column, row).get(XingDir.N_IN);
				Vertex sOut = intersections.get(column, row).get(XingDir.S_OUT);
				Vertex sIn = intersections.get(column, row).get(XingDir.S_IN);
				Vertex eOut = intersections.get(column, row).get(XingDir.E_OUT);
				Vertex eIn = intersections.get(column, row).get(XingDir.E_IN);
				Vertex wOut = intersections.get(column, row).get(XingDir.W_OUT);
				Vertex wIn = intersections.get(column, row).get(XingDir.W_IN);

				if (row > 0) {

					edge = new Edge(length);
					edge.setSpeedLimit(speedLimit);
					edge.setAccessController(trafficLights[column][row - 1].getController(0));
					edge.setTrafficLight(trafficLights[column][row - 1]);
					network.addEdge(nOut, intersections.get(column, row - 1).get(XingDir.S_IN), edge);

					edge = new Edge(length);
					edge.setSpeedLimit(speedLimit);
					edge.setAccessController(trafficLights[column][row].getController(0));
					edge.setTrafficLight(trafficLights[column][row]);
					network.addEdge(intersections.get(column, row - 1).get(XingDir.S_OUT), nIn, edge);

					if (row < rows - 1) {
						addEdgeToNetwork(network, nIn, sOut, length, speedLimit);
					}

					if (column < columns - 1) {
						addTurnEdgeToNetwork(network, nIn, eOut, length);
					}

					if (column > 0) {
						addTurnEdgeToNetwork(network, nIn, wOut, length);
					}
				}

				if (column > 0) {
					edge = new Edge(length);
					edge.setSpeedLimit(speedLimit);
					edge.setAccessController(trafficLights[column - 1][row].getController(1));
					edge.setTrafficLight(trafficLights[column - 1][row]);
					network.addEdge(wOut, intersections.get(column - 1, row).get(XingDir.E_IN), edge);

					edge = new Edge(length);
					edge.setSpeedLimit(speedLimit);
					edge.setAccessController(trafficLights[column][row].getController(1));
					edge.setTrafficLight(trafficLights[column][row]);
					network.addEdge(intersections.get(column - 1, row).get(XingDir.E_OUT), wIn, edge);

					if (column < columns - 1) {
						addEdgeToNetwork(network, wIn, eOut, length, speedLimit);
					}

					if (row < rows - 1) {
						addTurnEdgeToNetwork(network, wIn, sOut, length);
					}

					if (row > 0) {
						addTurnEdgeToNetwork(network, wIn, nOut, length);
					}
				}

				if (column < columns - 1) {
					if (row < rows - 1) {
						addTurnEdgeToNetwork(network, eIn, sOut, length);
					}

					if (row > 0) {
						addTurnEdgeToNetwork(network, eIn, nOut, length);
					}

					if (column > 0) {
						addEdgeToNetwork(network, eIn, wOut, length, speedLimit);
					}
				}

				if (row < rows - 1) {
					if (column < columns - 1) {
						addTurnEdgeToNetwork(network, sIn, eOut, length);
					}

					if (row > 0) {
						addEdgeToNetwork(network, sIn, nOut, length, speedLimit);
					}

					if (column > 0) {
						addTurnEdgeToNetwork(network, sIn, wOut, length);
					}
				}

			}
		}

		return network;
	}

	private void addEdgeToNetwork(RoadNetwork network, Vertex in, Vertex out, double length, double speedLimit) {
		Edge edge;
		edge = new Edge(length);
		edge.setSpeedLimit(speedLimit);
		network.addEdge(in, out, edge);
	}

	private void addTurnEdgeToNetwork(RoadNetwork network, Vertex in, Vertex out, double length) {
		Edge edge;
		edge = new Edge(length);
		edge.setSpeedLimit(TURN_SPEED_LIMIT);
		network.addEdge(in, out, edge);
	}
}
