package uk.ac.manchester.trafford.gui;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import uk.ac.manchester.trafford.network.edge.Edge;

public class StreetPopupController implements Initializable {
	private static final Logger _logger = LogManager.getLogger(TrafficLightPopupController.class);

	@FXML
	protected ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	protected TextField speedLimit;

	@FXML
	protected Button ok;

	@FXML
	protected Button cancel;

	private FloatProperty speedLimitNumber = new SimpleFloatProperty(1);

	protected Edge mStreet = null;

	/**
	 * Constructor
	 * 
	 * @param trafficlight
	 */
	public StreetPopupController(Edge street) {
		_logger.debug(">>> ENTER: TrafficLightPopupController");
		mStreet = street;
		if (mStreet != null) {
			_logger.debug("trafficlight is not null!");
			speedLimitNumber.set((float) mStreet.getSpeedLimit());
		}
		_logger.debug("<<< EXIT: TrafficLightPopupController");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		_logger.debug(">>> ENTER: initialize");
		Bindings.bindBidirectional(speedLimit.textProperty(), speedLimitNumber, new NumberStringConverter());

		ok.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				_logger.debug(">>> ENTER: ok");
				if (mStreet != null) {
					mStreet.setSpeedLimit(speedLimitNumber.floatValue());
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
