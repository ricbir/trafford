package uk.ac.manchester.trafford;

import javafx.scene.layout.Pane;

public class ResizablePane extends Pane {
	public ResizablePane() {
	}

	@Override
	public boolean isResizable() {
		return true;
	}
}