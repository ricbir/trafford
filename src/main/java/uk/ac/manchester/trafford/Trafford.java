package uk.ac.manchester.trafford;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Trafford extends Application {
	private static final Logger _logger = LogManager.getLogger(Trafford.class);
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

	public static void main(String[] args) {
		DOMConfigurator.configure(Trafford.class.getResource("log4j.xml"));
		_logger.debug(">>> ENTER: main");

		launch();
		_logger.debug("<<< EXIT: main");
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));

		Scene scene = new Scene(root);

		stage.setTitle("Trafford: Traffic Simulator");
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() {
		EXECUTOR_SERVICE.shutdownNow();
	}
}
