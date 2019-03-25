package uk.ac.manchester.trafford;

import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.gui.StreetPopupController;
import uk.ac.manchester.trafford.gui.TrafficLightPopupController;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.Edge;

public class PaneRenderer implements Renderer<RoadNetwork> {
	private static final Logger _logger = LogManager.getLogger(Main.class);

	private Pane gc;
	private RoadNetwork network;
	private double scalingFactor = 0.02;
	private double translateX = 10;
	private double translateY = 10;

	/**
	 * Contains mapping in between Edge and Circle
	 */
	protected Hashtable<Edge, Circle> mTrafficLightDict = new Hashtable<Edge, Circle>();
	protected Hashtable<Edge, Line> mStreetDict = new Hashtable<Edge, Line>();
	protected Hashtable<Agent, Rectangle> mAgentDict = new Hashtable<Agent, Rectangle>();

	public PaneRenderer(Pane pane) {
		this.gc = pane;
	}

	private void renderTrafficLight(Edge edge, Point source, Point target, Color color) {
		double xOffset = (source.getY() - target.getY()) / edge.getLength();
		double yOffset = (target.getX() - source.getX()) / edge.getLength();
		double x = xOffset - yOffset + target.getX();
		double y = yOffset + xOffset + target.getY();
		// gc.setFill(color);
		// gc.fillOval(x - 40, y - 40, 80, 80);
		if (mTrafficLightDict.containsKey(edge)) {
			Circle c = mTrafficLightDict.get(edge);
			c.setCenterX(scalingFactor * x);
			c.setCenterY(scalingFactor * y);
			c.setFill(color);
		} else {
			Circle c = new Circle(scalingFactor * x, scalingFactor * y, 3);
			c.setFill(color);
			mTrafficLightDict.put(edge, c);
			gc.getChildren().add(c);
			c.addEventHandler(MouseEvent.MOUSE_RELEASED, (e) -> {
				_logger.debug(">>> ENTER: MouseEvent.MOUSE_RELEASED");
				if (e.getButton() == MouseButton.SECONDARY) {
					_logger.debug("Right button down");
					Circle theCircle = (Circle) e.getSource();
					if (mTrafficLightDict.containsValue(theCircle)) {
						Edge targetEdge = null;
						for (Edge edg : mTrafficLightDict.keySet()) {
							if (mTrafficLightDict.get(edg).equals(theCircle)) {
								targetEdge = edg;
								break;
							}
						}
						if (targetEdge != null) {
							_logger.debug("Target edge found!");

							try {
								TrafficLightPopupController controller = new TrafficLightPopupController(
										targetEdge.getTrafficLight());
								FXMLLoader loader = new FXMLLoader(getClass().getResource("trafficlightconf.fxml"));
								// Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
								loader.setController(controller);
								Parent root1 = (Parent) loader.load();
								Stage stage = new Stage();
								stage.initModality(Modality.APPLICATION_MODAL);
								stage.initStyle(StageStyle.UNIFIED);
								stage.setResizable(false);
								stage.setTitle("Trafficlight");
								stage.setScene(new Scene(root1));
								stage.show();
							} catch (Exception ex) {
								_logger.error(ex);
								ex.printStackTrace();
							}
						}
					}
				}
				_logger.debug("<<< EXIT: MouseEvent.MOUSE_RELEASED");
			});
		}
	}

	@Override
	public void render() {

		gc.setTranslateX(translateX);
		gc.setTranslateY(translateY);

		for (Edge edge : network.edgeSet()) {
			// gc.setLineWidth(20);
			Point source = network.getEdgeSource(edge);
			Point target = network.getEdgeTarget(edge);
			// gc.strokeLine(source.getX(), source.getY(), target.getX(), target.getY());
			renderStreet(edge, source.getX(), source.getY(), target.getX(), target.getY());

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

		for (Agent a : mAgentDict.keySet()) {
			Rectangle r = mAgentDict.get(a);
			gc.getChildren().remove(r);
		}
		mAgentDict.clear();

		// gc.setFill(Color.DODGERBLUE);
		for (Agent agent : network.agentSetSnapshot()) {
			Point point = network.getCoordinates(agent);
			if (point != null) {
				// gc.fillRect(point.getX() - 40, point.getY() - 40, 80, 80);
				// renderAgent(agent, point.getX() - 40, point.getY() - 40, 80, 80); // 80, 80);
				renderAgent(agent, point.getX(), point.getY());
			} else {
				if (mAgentDict.containsKey(agent)) {
					Rectangle r = mAgentDict.get(agent);
					gc.getChildren().remove(r);
					mAgentDict.remove(agent);
				}
			}
		}
		// gc.restore();
	}

	/**
	 * Render an agent as a small Circle
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @param l
	 */
	private void renderAgent(Agent agent, int x, int y) {
		if (mAgentDict.containsKey(agent)) {
			Rectangle r = mAgentDict.get(agent);
			r.setX(scalingFactor * (x - agent.getSize() / 2));
			r.setY(scalingFactor * (y - agent.getSize() / 2));
			r.setWidth(scalingFactor * agent.getSize());
			r.setHeight(scalingFactor * agent.getSize());
		} else {
			Rectangle r = new Rectangle();
			r.setX(scalingFactor * (x - agent.getSize() / 2));
			r.setY(scalingFactor * (y - agent.getSize() / 2));
			r.setWidth(scalingFactor * agent.getSize());
			r.setHeight(scalingFactor * agent.getSize());
			mAgentDict.put(agent, r);
			gc.getChildren().add(r);
		}
	}

	/**
	 * Render the street as a Line object
	 * 
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 */
	private void renderStreet(Edge edge, int x, int y, int x2, int y2) {
		if (mStreetDict.containsKey(edge)) {
			Line l = mStreetDict.get(edge);
			l.setStartX(scalingFactor * x);
			l.setStartY(scalingFactor * y);
			l.setEndX(scalingFactor * x2);
			l.setEndY(scalingFactor * y2);
		} else {
			Line l = new Line();
			l.setStrokeWidth(1);
			l.setStartX(scalingFactor * x);
			l.setStartY(scalingFactor * y);
			l.setEndX(scalingFactor * x2);
			l.setEndY(scalingFactor * y2);
			mStreetDict.put(edge, l);
			gc.getChildren().add(l);

			l.addEventHandler(MouseEvent.MOUSE_RELEASED, (e) -> {
				_logger.debug(">>> ENTER: MouseEvent.MOUSE_RELEASED");
				if (e.getButton() == MouseButton.SECONDARY) {
					_logger.debug("Right button down");
					Line theLine = (Line) e.getSource();
					if (mStreetDict.containsValue(theLine)) {
						Edge targetEdge = null;
						for (Edge edg : mStreetDict.keySet()) {
							if (mStreetDict.get(edg).equals(theLine)) {
								targetEdge = edg;
								break;
							}
						}
						if (targetEdge != null) {
							_logger.debug("Target edge found!");

							try {
								StreetPopupController controller = new StreetPopupController(targetEdge);
								FXMLLoader loader = new FXMLLoader(getClass().getResource("streetconf.fxml"));
								// Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
								loader.setController(controller);
								Parent root1 = (Parent) loader.load();
								Stage stage = new Stage();
								stage.initModality(Modality.APPLICATION_MODAL);
								stage.initStyle(StageStyle.UNIFIED);
								stage.setResizable(false);
								stage.setTitle("Street");
								stage.setScene(new Scene(root1));
								stage.show();
							} catch (Exception ex) {
								_logger.error(ex);
								ex.printStackTrace();
							}
						}
					}
				}
				_logger.debug("<<< EXIT: MouseEvent.MOUSE_RELEASED");
			});

		}
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
		if (scalingFactor < 0) {
			return;
		}
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
		if (translateX < 10) {
			return;
		}
		this.translateX = translateX;
	}

	@Override
	public double getTranslateY() {
		return translateY;
	}

	@Override
	public void setTranslateY(double translateY) {
		if (translateY < 10) {
			return;
		}
		this.translateY = translateY;
	}
}