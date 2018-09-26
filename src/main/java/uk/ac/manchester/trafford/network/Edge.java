package uk.ac.manchester.trafford.network;

import org.jgrapht.graph.DefaultWeightedEdge;

import uk.ac.manchester.trafford.util.Convert;

public class Edge extends DefaultWeightedEdge {

	private static final long serialVersionUID = -4142216540556919212L;

	private int length = 1;
	private int speedLimit = Convert.metersToMillimeters(Convert.kmphToMps(50));

	/**
	 * @param length     The length of the edge in mm
	 * @param speedLimit The speed limit in mm/s
	 * 
	 */
	public Edge(int length, int speedLimit) {
		this.length = length;
		this.speedLimit = speedLimit;
	}

	public int getLength() {
		return length;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

}
