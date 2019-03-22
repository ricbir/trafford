package uk.ac.manchester.trafford;

import javafx.scene.canvas.Canvas;

/**
 * Source: https://dlsc.com/2014/04/10/javafx-tip-1-resizable-canvas/
 *
 */
public class ResizableCanvas extends Canvas {
	public ResizableCanvas() {
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}
}
