package uk.ac.manchester.trafford.gui;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import uk.ac.manchester.trafford.network.edge.TimedTrafficLight;

public class TrafficLightPopupController implements Initializable {
	private static final Logger _logger = LogManager.getLogger(TrafficLightPopupController.class);

	@FXML
	protected ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	protected TextField greenSeconds;

	@FXML
	protected TextField yellowSeconds;

	@FXML
	protected Button ok;

	@FXML
	protected Button cancel;

	private IntegerProperty greenSecondsNumber = new SimpleIntegerProperty(1);
	private IntegerProperty yellowSecondsNumber = new SimpleIntegerProperty(1);

	protected TimedTrafficLight mTrafficLight = null;

	/**
	 * Constructor
	 * 
	 * @param trafficlight
	 */
	public TrafficLightPopupController(TimedTrafficLight trafficlight) {
		_logger.debug(">>> ENTER: TrafficLightPopupController");
		mTrafficLight = trafficlight;
		if (mTrafficLight != null) {
			_logger.debug("trafficlight is not null!");
			greenSecondsNumber.set(mTrafficLight.GetGreenSeconds());
			yellowSecondsNumber.set(mTrafficLight.GetYellowSeconds());
		}
		_logger.debug("<<< EXIT: TrafficLightPopupController");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_logger.debug(">>> ENTER: initialize");
		Bindings.bindBidirectional(greenSeconds.textProperty(), greenSecondsNumber, new NumberStringConverter());
		Bindings.bindBidirectional(yellowSeconds.textProperty(), yellowSecondsNumber, new NumberStringConverter());

		ok.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				_logger.debug(">>> ENTER: ok");
				if (mTrafficLight != null) {
					mTrafficLight.SetGreenSeconds(greenSecondsNumber.intValue());
					mTrafficLight.SetYellowSeconds(yellowSecondsNumber.intValue());
				}
				// Close the popup window
				((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
				_logger.debug("<<< EXIT: ok");
			}
		});

		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				_logger.debug(">>> ENTER: cancel");
				((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
				_logger.debug("<<< EXIT: cancel");
			}
		});

		_logger.debug("<<< EXIT: initialize");
	}

	/**
	 * Invoke to close the popup
	 */
	protected void ClosePopup() {
		_logger.debug(">>> ENTER: ClosePopup");
		_logger.debug("<<< EXIT: ClosePopup");
	}

}
