package uk.ac.manchester.trafford;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.text.Text;
import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.agent.IDMAccelerator;
import uk.ac.manchester.trafford.agent.Position;
import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;
import uk.ac.manchester.trafford.network.SegmentConnection;
import uk.ac.manchester.trafford.network.factories.RoadNetworkFactory;
import uk.ac.manchester.trafford.network.factories.SimpleGridRoadNetworkFactory;

public class ApplicationController implements Initializable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(ApplicationController.class);

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private MenuBar menuBar;

	@FXML
	private Pane simulationPane;

	@FXML
	private Slider speedSlider;

	@FXML
	private ToggleButton runButton;

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

	private DoubleProperty scalingFactor = new SimpleDoubleProperty(1);
	private DoubleProperty translateXProperty = new SimpleDoubleProperty(10);
	private DoubleProperty translateYProperty = new SimpleDoubleProperty(10);

	private RoadNetwork network;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		RoadNetworkFactory factory = new SimpleGridRoadNetworkFactory(3, 3, 100);
		network = factory.buildRoadNetwork();

		Pane simulationView = new AnchorPane();
		simulationView.scaleXProperty().bind(scalingFactor);
		simulationView.scaleYProperty().bind(scalingFactor);
		simulationView.translateXProperty().bind(translateXProperty);
		simulationView.translateYProperty().bind(translateYProperty);
		simulationPane.getChildren().add(simulationView);

		for (Segment segment : network.vertexSet()) {
			Point source = segment.getSource();
			Point target = segment.getTarget();
			Line roadSegment = new Line();
			roadSegment.setStartX(source.getX());
			roadSegment.setStartY(source.getY());

			roadSegment.setEndX(target.getX());
			roadSegment.setEndY(target.getY());
			roadSegment.setStroke(Color.LIGHTGREY);
			roadSegment.setStrokeWidth(2.5);

			simulationView.getChildren().add(roadSegment);
		}

		Agent agent;
		try {
			agent = new Agent(network, Position.create(new Segment(Point.create(0, 0), Point.create(0, 100)), 50),
					Position.create(new Segment(Point.create(100, 200), Point.create(200, 200)), 50),
					new DijkstraShortestPath<Segment, SegmentConnection>(network),
					new IDMAccelerator(Constants.AGENT_ACCELERATION, Constants.AGENT_DECELERATION,
							Constants.DESIRED_TIME_HEADWAY, Constants.MINIMUM_SPACING));

			Circle agentSprite = new Circle(1, Color.DODGERBLUE);
			simulationView.getChildren().add(agentSprite);

			AnimationTimer timer = new AnimationTimer() {

				private long lastUpdateTime = System.nanoTime();

				@Override
				public void handle(long now) {
					long timeBetweenUpdates;

					while (now - lastUpdateTime > (timeBetweenUpdates = (long) (Constants.NANOSECONDS_PER_SECOND
							/ Constants.UPDATES_PER_SECOND / speedSlider.getValue()))) {

						agent.update();
						agentSprite.setCenterX(agent.getX());
						agentSprite.setCenterY(agent.getY());
						lastUpdateTime += timeBetweenUpdates;
					}
					// congestionCoefficient.setText(String.format("%.4f",
					// network.getAverageCongestion()));
				}
			};

			timer.start();
		} catch (DistanceOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
}
