package uk.ac.manchester.trafford;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.GraphExporter;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.gui.TrafficLightPopupController;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.RoadNetworkFactory;
import uk.ac.manchester.trafford.network.Vertex;
import uk.ac.manchester.trafford.network.edge.Edge;
import uk.ac.manchester.trafford.network.edge.EdgePosition;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;

public class ApplicationController implements Initializable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ApplicationController.class);

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	protected MenuBar menuBar;

	private Canvas agentCanvas;

	@FXML
	private Pane simulationPane;

	@FXML
	private Slider speedSlider;

	@FXML
	private ToggleButton runButton;

	private Renderer<Iterable<Agent>> renderer;
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
	private TextField globalSpeedLimitField;

	@FXML
	private Button randomizeTimingsButton;

	@FXML
	private MenuItem close;

	@FXML
	private Text congestionCoefficient;

	private IntegerProperty agentNumber = new SimpleIntegerProperty(1);
	private IntegerProperty agentSpawnRate = new SimpleIntegerProperty(1);

	private double lastMouseX = 0;
	private double lastMouseY = 0;

	private DoubleProperty scalingFactor = new SimpleDoubleProperty(0.5);
	private DoubleProperty translateXProperty = new SimpleDoubleProperty(0);
	private DoubleProperty translateYProperty = new SimpleDoubleProperty(0);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LOGGER.debug(">>> ENTER initialize (" + location + ", " + resources + ")");
		Bindings.bindBidirectional(nAgentsField.textProperty(), agentNumber, new NumberStringConverter());
		Bindings.bindBidirectional(spawnRateField.textProperty(), agentSpawnRate, new NumberStringConverter());

		close.setOnAction(e -> {
			Platform.exit();
		});

		agentSpeedField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [agentSpeedField.onAction] (" + e + ")");
			try {
				network.setAgentSpeed(Double.parseDouble(agentSpeedField.getText()));
			} catch (NumberFormatException nfe) {
				LOGGER.info("Could not parse \"" + agentSpeedField.getText() + "\"", nfe);
				agentSpeedField.setText(Double.toString(network.getAgentSpeed()));
			}
			LOGGER.debug("<<< EXIT [agentSpeedField.onAction]");
		});

		agentSpeedVariabilityField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [agentSpeedVariabilityField.onAction] (" + e + ")");
			try {
				network.setAgentSpeedVariability(Double.parseDouble(agentSpeedVariabilityField.getText()) / 100);
			} catch (NumberFormatException nfe) {
				LOGGER.info("Could not parse \"" + agentSpeedVariabilityField.getText() + "\"", nfe);
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
				LOGGER.info("Could not parse \"" + greenTimeField.getText() + "\"", nfe);
				greenTimeField.setText(Double.toString(0));
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
				LOGGER.info("Could not parse \"" + yellowTimeField.getText() + "\"", nfe);
				yellowTimeField.setText(Double.toString(0));
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

		globalSpeedLimitField.setOnAction(e -> {
			LOGGER.debug(">>> ENTER [globalSpeedLimitField.onAction] (" + e + ")");
			double speedLimit;
			try {
				speedLimit = Double.parseDouble(globalSpeedLimitField.getText());
				for (Edge edge : network.edgeSet()) {
					edge.setSpeedLimit(speedLimit);
					;
				}
			} catch (NumberFormatException nfe) {
				LOGGER.info("Could not parse \"" + globalSpeedLimitField.getText() + "\"", nfe);
				yellowTimeField.setText(Double.toString(0));
			}
			LOGGER.debug("<<< EXIT [globalSpeedLimitField.onAction]");
		});

		simulationPane.setOnScroll(e -> {
			double scrollAmount = e.getDeltaY();
			scalingFactor.set(scalingFactor.get() + scrollAmount * 0.005);
		});

		network = RoadNetworkFactory.getFactory().grid(8, 8, 100, 15);

		Rectangle clipMask = new Rectangle();
		clipMask.widthProperty().bind(simulationPane.widthProperty());
		clipMask.heightProperty().bind(simulationPane.heightProperty());
		simulationPane.setClip(clipMask);

		Pane simulationView = new AnchorPane();
		simulationView.scaleXProperty().bind(scalingFactor);
		simulationView.scaleYProperty().bind(scalingFactor);
		simulationView.translateXProperty().bind(translateXProperty);
		simulationView.translateYProperty().bind(translateYProperty);

		// Draw edges
		for (Edge edge : network.edgeSet()) {
			Vertex source = network.getEdgeSource(edge);
			Vertex target = network.getEdgeTarget(edge);
			Line roadSegment = new Line();
			roadSegment.startXProperty().bind(source.getXProperty());
			roadSegment.startYProperty().bind(source.getYProperty());
			roadSegment.endXProperty().bind(target.getXProperty());
			roadSegment.endYProperty().bind(target.getYProperty());
			roadSegment.setStroke(Color.LIGHTGREY);
			roadSegment.setStrokeWidth(2.5);

			simulationView.getChildren().add(roadSegment);
		}

		agentCanvas = new Canvas(0, 0);
		agentCanvas.widthProperty().bind(simulationView.widthProperty());
		agentCanvas.heightProperty().bind(simulationView.heightProperty());
		simulationView.getChildren().add(agentCanvas);

		// Draw traffic light
		for (Edge edge : network.edgeSet()) {
			if (edge.getAccessController() instanceof TimedTrafficLight.AccessController) {
				Circle trafficLight = new Circle(1.5, Color.RED);

				Vertex source = network.getEdgeSource(edge);
				Vertex target = network.getEdgeTarget(edge);

				// Draw it on the right-hand side of the segment
				DoubleBinding xOffset = source.getYProperty().subtract(target.getYProperty()).divide(edge.getLength())
						.multiply(4);
				DoubleBinding yOffset = target.getXProperty().subtract(source.getXProperty()).divide(edge.getLength())
						.multiply(4);
				trafficLight.centerXProperty().bind(target.getXProperty().add(xOffset.subtract(yOffset)));
				trafficLight.centerYProperty().bind(target.getYProperty().add(yOffset.add(xOffset)));

				edge.getAccessController().getObservableState().addListener((observable, oldValue, newValue) -> {
					switch (newValue) {
					case TL_GREEN:
						trafficLight.setFill(Color.LIMEGREEN);
						break;
					case TL_RED:
						trafficLight.setFill(Color.RED);
						break;
					case TL_YELLOW:
						trafficLight.setFill(Color.GOLDENROD);
						break;
					default:
						break;
					}
				});

				Circle clickableArea = new Circle(4, Color.TRANSPARENT);
				clickableArea.centerXProperty().bind(trafficLight.centerXProperty());
				clickableArea.centerYProperty().bind(trafficLight.centerYProperty());
				clickableArea.setCursor(Cursor.HAND);
				clickableArea.setOnMouseClicked((e) -> {
					try {
						TrafficLightPopupController controller = new TrafficLightPopupController(
								edge.getTrafficLight());
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
						LOGGER.error(ex, ex);
					}
				});

				simulationView.getChildren().add(trafficLight);
				simulationView.getChildren().add(clickableArea);
			}
		}

		simulationPane.setOnMousePressed(e -> {
			LOGGER.debug(">>> ENTER [simulationPane.setOnMousePressed] (" + e + ")");
			lastMouseX = e.getX();
			lastMouseY = e.getY();
			simulationPane.setCursor(Cursor.CLOSED_HAND);
			LOGGER.debug("<<< EXIT [simulationPane.setOnMousePressed]");
		});

		simulationPane.setOnMouseDragged(e -> {
			translateXProperty.set(translateXProperty.get() + e.getX() - lastMouseX);
			translateYProperty.set(translateYProperty.get() + e.getY() - lastMouseY);
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		});

		simulationPane.setOnMouseReleased(e -> {
			LOGGER.debug(">>> ENTER [simulationPane.setOnMousePressed] (" + e + ")");
			simulationPane.setCursor(Cursor.OPEN_HAND);
			LOGGER.debug("<<< EXIT [simulationPane.setOnMousePressed]");
		});

		simulationPane.setCursor(Cursor.OPEN_HAND);

		simulationPane.getChildren().add(simulationView);

		renderer = new Renderer<Iterable<Agent>>() {
			@Override
			public void render(GraphicsContext gc, Iterable<Agent> agents) {
				gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
				gc.save();

				gc.setFill(Color.DODGERBLUE);
				for (Agent agent : agents) {
					Vertex point = network.getCoordinates(agent);
					if (point != null) {
						gc.fillRect(point.getX() - 1, point.getY() - 1, 2, 2);
					}
				}
				gc.restore();
			}
		};

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

		Trafford.EXECUTOR_SERVICE.execute(updateModelTask);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				renderer.render(agentCanvas.getGraphicsContext2D(), Arrays.asList(network.agentSetSnapshot()));
				congestionCoefficient.setText(String.format("%.4f", network.getAverageCongestion()));
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
			showMessage("WARNING", "Action error", "Cannot open network while the simulation is running.");
			LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File file = fileChooser.showOpenDialog(simulationPane.getScene().getWindow());

		LOGGER.debug("Opening file [" + file + "]");

		try {
			network = RoadNetworkFactory.getFactory().importFromFile(file);
		} catch (Exception ex) {
			LOGGER.error(ex, ex);
		}

		LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
	}

	@FXML
	private void handleMenuSaveAction(ActionEvent event) {
		LOGGER.debug(">>> ENTER [handleMenuSaveAction]");
		if (runButton.isSelected()) {
			showMessage("WARNING", "Action error", "Cannot save network while the simulation is running.");
			LOGGER.debug("<<< EXIT [handleMenuOpenAction]");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.setInitialFileName("graph.dot");
		File file = fileChooser.showSaveDialog(simulationPane.getScene().getWindow());

		LOGGER.debug("Saving to file [" + file + "]");

		try {
			GraphExporter<Vertex, Edge> exporter = new DOTExporter<>(Vertex.idProvider(), Vertex.labelProvider(), null,
					Vertex.attributeProvider(), Edge.attributeProvider());
			exporter.exportGraph(network, file);
		} catch (Exception ex) {
			LOGGER.error(ex);
		}

		LOGGER.debug("<<< EXIT [handleMenuSaveAction]");
	}
}
