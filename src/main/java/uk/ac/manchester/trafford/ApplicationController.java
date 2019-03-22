package uk.ac.manchester.trafford;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.util.converter.NumberStringConverter;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetworkBuilder;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;

public class ApplicationController implements Initializable {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

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
	private TextField nAgentsField;

	@FXML
	private TextField spawnRateField;

	@FXML
	private TextField agentSpeedField;

	@FXML
	private TextField agentSpeedVariabilityField;

	private IntegerProperty agentNumber = new SimpleIntegerProperty(1);
	private IntegerProperty agentSpawnRate = new SimpleIntegerProperty(1);

	private double lastMouseX = 0;
	private double lastMouseY = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Bindings.bindBidirectional(nAgentsField.textProperty(), agentNumber, new NumberStringConverter());
		Bindings.bindBidirectional(spawnRateField.textProperty(), agentSpawnRate, new NumberStringConverter());

		agentSpeedField.setOnAction(e -> {
			try {
				network.setAgentSpeed(Double.parseDouble(agentSpeedField.getText()));
			} catch (NumberFormatException nfe) {
				agentSpeedField.setText(Double.toString(network.getAgentSpeed()));
			}
		});

		agentSpeedVariabilityField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				network.setAgentSpeedVariability(Double.parseDouble(newValue));
			} catch (NumberFormatException e) {
				((StringProperty) observable).set(oldValue);
			}
		});

		simulationCanvas = new ResizableCanvas();
		simulationCanvas.setCursor(Cursor.OPEN_HAND);
		simulationCanvas.widthProperty().bind(simulationPane.widthProperty());
		simulationCanvas.heightProperty().bind(simulationPane.heightProperty());
		simulationPane.getChildren().add(simulationCanvas);

		simulationPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		});

		simulationPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			renderer.setTranslateX(renderer.getTranslateX() + e.getX() - lastMouseX);
			renderer.setTranslateY(renderer.getTranslateY() + e.getY() - lastMouseY);
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		});

		simulationPane.addEventHandler(ScrollEvent.SCROLL, e -> {
			double scrollAmount = e.getDeltaY();
			renderer.setScalingFactor(renderer.getScalingFactor() + (scrollAmount * 0.00005));
		});

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
							for (int i = 0; (i < Math.min(agentNumber.get() - network.getNumberOfAgents(),
									agentSpawnRate.get())); i++) {
								edges = network.edgeSet().toArray(edges);
								int startEdgeIndex = (int) (Math.random() * edges.length);
								int targetEdgeIndex;
								do {
									targetEdgeIndex = (int) (Math.random() * edges.length);
								} while (targetEdgeIndex == startEdgeIndex);

								network.createAgent(
										new EdgePosition(edges[startEdgeIndex],
												edges[startEdgeIndex].getLength() * Math.random()),
										new EdgePosition(edges[targetEdgeIndex],
												edges[targetEdgeIndex].getLength() * Math.random()));
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
}
