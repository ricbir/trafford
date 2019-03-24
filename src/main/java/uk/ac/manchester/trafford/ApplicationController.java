package uk.ac.manchester.trafford;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetworkBuilder;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;
import uk.ac.manchester.trafford.pojo.EdgePojo;
import uk.ac.manchester.trafford.pojo.NetworkPojo;
import uk.ac.manchester.trafford.pojo.TrafficLightPojo;

public class ApplicationController implements Initializable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ApplicationController.class);

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	protected MenuBar menuBar;

	private Canvas simulationCanvas;

	private Pane simulationArea;

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

	@FXML
	private TextField greenTimeField;

	@FXML
	private TextField yellowTimeField;

	@FXML
	private Button randomizeTimingsButton;

	private IntegerProperty agentNumber = new SimpleIntegerProperty(1);
	private IntegerProperty agentSpawnRate = new SimpleIntegerProperty(1);

	private double lastMouseX = 0;
	private double lastMouseY = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug(">>> ENTER initialize (" + location + ", " + resources + ")");
		Bindings.bindBidirectional(nAgentsField.textProperty(), agentNumber, new NumberStringConverter());
		Bindings.bindBidirectional(spawnRateField.textProperty(), agentSpawnRate, new NumberStringConverter());

		agentSpeedField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [agentSpeedField.onAction] (" + e + ")");
			try {
				network.setAgentSpeed(Double.parseDouble(agentSpeedField.getText()));
			} catch (NumberFormatException nfe) {
				LOGGER.debug("Could not parse \"" + agentSpeedField.getText() + "\"", nfe);
				agentSpeedField.setText(Double.toString(network.getAgentSpeed()));
			}
			LOGGER.debug("<<< EXIT [agentSpeedField.onAction]");
		});

		agentSpeedVariabilityField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [agentSpeedVariabilityField.onAction] (" + e + ")");
			try {
				network.setAgentSpeedVariability(Double.parseDouble(agentSpeedVariabilityField.getText()) / 100);
			} catch (NumberFormatException nfe) {
				LOGGER.debug("Could not parse \"" + agentSpeedField.getText() + "\"", nfe);
				agentSpeedVariabilityField.setText(Double.toString(network.getAgentSpeedVariability() * 100));
			}
			LOGGER.debug("<<< EXIT [agentSpeedField.onAction]");
		});

		greenTimeField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [greenTimeField.onAction] (" + e + ")");
			double seconds;
			try {
				seconds = Double.parseDouble(greenTimeField.getText());
				for (TimedTrafficLight trafficLight : network.getTrafficLights()) {
					trafficLight.setGreenTime(seconds);
				}
				System.out.println("GREEN TIME: " + seconds);
			} catch (NumberFormatException nfe) {
				LOGGER.debug("Could not parse \"" + agentSpeedField.getText() + "\"", nfe);
				greenTimeField.setText(Double.toString(network.getAgentSpeed()));
			}
			LOGGER.debug("<<< EXIT [greenTimeField.onAction]");
		});

		yellowTimeField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [yellowTimeField.onAction] (" + e + ")");
			double seconds;
			try {
				seconds = Double.parseDouble(yellowTimeField.getText());
				for (TimedTrafficLight trafficLight : network.getTrafficLights()) {
					trafficLight.setYellowTime(seconds);
				}
			} catch (NumberFormatException nfe) {
				LOGGER.debug("Could not parse \"" + agentSpeedField.getText() + "\"", nfe);
				yellowTimeField.setText(Double.toString(network.getAgentSpeed()));
			}
			LOGGER.debug("<<< EXIT [yellowTimeField.onAction]");
		});

		randomizeTimingsButton.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [randomizeTimingsButton.onAction] (" + e + ")");
			for (TimedTrafficLight trafficLight : network.getTrafficLights()) {
				trafficLight.setGreenTime(Math.random() * 60);
			}
			LOGGER.debug("<<< EXIT [randomizeTimingsButton.onAction]");
		});

		simulationCanvas = new ResizableCanvas();
		simulationCanvas.setCursor(Cursor.OPEN_HAND);
		simulationCanvas.widthProperty().bind(simulationPane.widthProperty());
		simulationCanvas.heightProperty().bind(simulationPane.heightProperty());
		// simulationPane.getChildren().add(simulationCanvas);

		simulationArea = new ResizablePane();
		simulationArea.setBackground(new Background(new BackgroundFill(Color.AZURE, null, null)));
		// simulationArea.setCursor(Cursor.OPEN_HAND);
		simulationPane.getChildren().add(simulationArea);

		simulationPane.setOnMousePressed(e -> {
			LOGGER.debug(">>> ENTER [simulationPane.setOnMousePressed] (" + e + ")");
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			LOGGER.debug("<<< EXIT [simulationPane.setOnMousePressed]");
		});

		simulationPane.setOnMouseDragged(e -> {
			LOGGER.debug(">>> ENTER [simulationPane.setOnMouseDragged] (" + e + ")");
			renderer.setTranslateX(renderer.getTranslateX() + e.getX() - lastMouseX);
			renderer.setTranslateY(renderer.getTranslateY() + e.getY() - lastMouseY);
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			LOGGER.debug("<<< EXIT [simulationPane.setOnMouseDragged]");
		});

		simulationPane.setOnScroll(e -> {
			LOGGER.debug(">>> ENTER [simulationPane.setOnScroll] (" + e + ")");
			double scrollAmount = e.getDeltaY();
			renderer.setScalingFactor(renderer.getScalingFactor() + (scrollAmount * 0.00005));
			LOGGER.debug("<<< EXIT [simulationPane.setOnScroll]");
		});

		network = RoadNetworkBuilder.RoadNetwork().grid(8, 8, 100, 15).build();
		renderer = new PaneRenderer(simulationArea);
		renderer.setModel(network);

		Runnable updateModelTask = new Runnable() {
			private Edge[] edges = new Edge[1];
			private long lastUpdateTime = System.nanoTime();

			@Override
			public void run() {
				LOGGER.debug(">>> ENTER run");
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
				LOGGER.debug(">>> EXIT run");
			}
		};

		Main.EXECUTOR_SERVICE.execute(updateModelTask);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				renderer.render();
			}

		}.start();

		LOGGER.debug("<<< EXIT initialize");
	}

	/**
	 * Show an alert with given data
	 * 
	 * @param title
	 * @param header
	 * @param content
	 */
	public static void showMessage(String title, String header, String content) {
		LOGGER.debug(">>> ENTER [showMessage]");
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
		LOGGER.debug("<<< EXT [showMessage]");
	}

	@FXML
	private void handleMenuOpenAction(ActionEvent event) {
		LOGGER.debug(">>> ENTER [handleMenuOpenAction]");
		// If running this action cannot be executed
		if (runButton.isSelected()) {
			showMessage("WARNING", "Action error", "Cannot open network while running simulation!");
			LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File file = fileChooser.showOpenDialog(simulationPane.getScene().getWindow());

		LOGGER.debug("Opening file [" + file + "]");

		try {

			NetworkPojo net = NetworkPojo.Deserialize(file);

			for (Edge edge : this.network.edgeSet()) {
				EdgePojo ep = net.getEdgeById(edge.getId());
				if (ep != null) {
					edge.setSpeedLimith(ep.speedLimit);
					if (edge.getTrafficLight() != null) {
						TrafficLightPojo tlp = net.getTrafficlightById(edge.getTrafficLight().getId());
						if (tlp != null) {
							edge.getTrafficLight().SetGreenSeconds(tlp.greenSeconds);
							edge.getTrafficLight().SetYellowSeconds(tlp.yellowSeconds);
						}
					}
				}

			}

		} catch (Exception ex) {
			LOGGER.error(ex);
		}

		LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
	}

	@FXML
	private void handleMenuSaveAction(ActionEvent event) {
		LOGGER.debug(">>> ENTER [handleMenuSaveAction]");
		if (runButton.isSelected()) {
			showMessage("WARNING", "Action error", "Cannot save network while running simulation!");
			LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File file = fileChooser.showSaveDialog(simulationPane.getScene().getWindow());

		LOGGER.debug("Saving to file [" + file + "]");

		try {

			NetworkPojo net = new NetworkPojo();

			for (Edge edge : this.network.edgeSet()) {
				net.edgeList.add(new EdgePojo(edge.getId(), edge.getSpeedLimit()));
				if (edge.getTrafficLight() != null) {
					net.trafficlightList.add(new TrafficLightPojo(edge.getTrafficLight().getId(),
							edge.getTrafficLight().GetGreenSeconds(), edge.getTrafficLight().GetYellowSeconds()));
				}
			}

			NetworkPojo.Serialize(file, net);

		} catch (Exception ex) {
			LOGGER.error(ex);
		}

		LOGGER.debug("<<< EXIT [handleMenuSaveAction]");
	}
}
