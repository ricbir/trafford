package uk.ac.manchester.trafford;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.exceptions.NodeNotFoundException;
import uk.ac.manchester.trafford.exceptions.PathNotFoundException;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetworkBuilder;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class ApplicationController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Canvas simulationCanvas;

	@FXML
	private Pane simulationPane;

	@FXML
	private Slider speedSlider;

	@FXML
	private ToggleButton runButton;

	private Renderer<RoadNetwork> renderer;
	private RoadNetwork network;

	@FXML
	void onScroll(ScrollEvent event) {
		double scrollAmount = event.getDeltaY();
		renderer.setScalingFactor(renderer.getScalingFactor() + (scrollAmount * 0.00005));
	}

	double lastMouseX = 0;
	double lastMouseY = 0;

	@FXML
	void onMouseDragged(MouseEvent event) {
		renderer.setTranslateX(renderer.getTranslateX() + event.getX() - lastMouseX);
		renderer.setTranslateY(renderer.getTranslateY() + event.getY() - lastMouseY);
		lastMouseX = event.getX();
		lastMouseY = event.getY();
	}

	@FXML
	void onMousePressed(MouseEvent event) {
		lastMouseX = event.getX();
		lastMouseY = event.getY();
	}

	@FXML
	void initialize() {
		assert simulationCanvas != null : "fx:id=\"simulationCanvas\" was not injected: check your FXML file 'main.fxml'.";

		simulationCanvas.heightProperty().bind(simulationPane.heightProperty());
		simulationCanvas.widthProperty().bind(simulationPane.widthProperty());

		network = RoadNetworkBuilder.RoadNetwork().grid(3, 3, 100, 15).build();
		renderer = new CanvasRenderer(simulationCanvas.getGraphicsContext2D());

		renderer.setModel(network);

		Runnable updateModelTask = new Runnable() {
			private Edge[] edges = new Edge[1];
			private long lastUpdateTime = System.nanoTime();

			@Override
			public void run() {
				while (!Thread.interrupted()) {
					long now = System.nanoTime();
					long timeBetweenUpdates;

					while (now - lastUpdateTime > (timeBetweenUpdates = (long) (Constants.NANOSECONDS_PER_SECOND
							/ Constants.UPDATES_PER_SECOND / speedSlider.getValue()))) {
						if (runButton.isSelected()) {
							if (network.agentSetSnapshot().length < 1000) {
								edges = network.edgeSet().toArray(edges);
								int startEdgeIndex = (int) (Math.random() * edges.length);
								int destinationEdgeIndex;
								do {
									destinationEdgeIndex = (int) (Math.random() * edges.length);
								} while (destinationEdgeIndex == startEdgeIndex);

								addAgent(new EdgePosition(edges[startEdgeIndex], edges[startEdgeIndex].getLength() / 2),
										new EdgePosition(edges[destinationEdgeIndex],
												edges[destinationEdgeIndex].getLength() / 2),
										Math.random() * 20 + 10);
							}

							network.update();
						}
						lastUpdateTime += timeBetweenUpdates;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};

		Main.EXECUTOR_SERVICE.execute(updateModelTask);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				renderer.render();
			}

		}.start();
	}

	public void addAgent(EdgePosition source, EdgePosition target, double maxSpeed) {
		try {
			network.addAgent(new Agent(network, source, target, maxSpeed));
		} catch (PathNotFoundException | NodeNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
