package uk.ac.manchester.trafford;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.Edge;

public class CanvasRenderer implements Renderer<RoadNetwork> {

	private GraphicsContext gc;
	private RoadNetwork network;
	private double scalingFactor = 0.01;
	private double translateX = 0;
	private double translateY = 0;

	public CanvasRenderer(GraphicsContext gc) {
		this.gc = gc;
	}

	private void renderTrafficLight(Edge edge, Point source, Point target, Color color) {
		double xOffset = (source.getY() - target.getY()) / edge.getLength();
		double yOffset = (target.getX() - source.getX()) / edge.getLength();
		double x = xOffset - yOffset + target.getX();
		double y = yOffset + xOffset + target.getY();
		gc.setFill(color);
		gc.fillOval(x - 40, y - 40, 80, 80);
	}

	@Override
	public void render() {
		// clear canvas
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		gc.save();
		gc.setStroke(Color.DARKGREY);

		gc.translate(translateX, translateY);
		gc.scale(scalingFactor, scalingFactor);

		for (Edge edge : network.edgeSet()) {
			gc.setLineWidth(20);
			Point source = network.getEdgeSource(edge);
			Point target = network.getEdgeTarget(edge);
			gc.strokeLine(source.getX(), source.getY(), target.getX(), target.getY());

			switch (edge.getAccessState()) {
			case TL_GREEN:
				renderTrafficLight(edge, source, target, Color.GREEN);
				break;
			case TL_YELLOW:
				renderTrafficLight(edge, source, target, Color.GOLDENROD);
				break;
			case TL_RED:
				renderTrafficLight(edge, source, target, Color.RED);
				break;
			default:
				break;
			}
		}

		gc.setFill(Color.DODGERBLUE);
		for (Agent agent : network.agentSetSnapshot()) {
			Point point = network.getCoordinates(agent);
			if (point != null) {
				gc.fillRect(point.getX() - 40, point.getY() - 40, 80, 80);
			}
		}
		gc.restore();
	}

	@Override
	public RoadNetwork getModel() {
		return network;
	}

	@Override
	public void setModel(RoadNetwork network) {
		this.network = network;
	}

	@Override
	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	@Override
	public double getScalingFactor() {
		return scalingFactor;
	}

	@Override
	public double getTranslateX() {
		return translateX;
	}

	@Override
	public void setTranslateX(double translateX) {
		this.translateX = translateX;
	}

	@Override
	public double getTranslateY() {
		return translateY;
	}

	@Override
	public void setTranslateY(double translateY) {
		this.translateY = translateY;
	}

}
