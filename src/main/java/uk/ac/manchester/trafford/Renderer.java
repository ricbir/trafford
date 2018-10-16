package uk.ac.manchester.trafford;

import uk.ac.manchester.trafford.network.RoadNetwork;

public interface Renderer {
	public void render(double interpolation);

	public void setNetwork(RoadNetwork network);
}
