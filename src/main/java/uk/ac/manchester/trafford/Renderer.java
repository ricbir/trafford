package uk.ac.manchester.trafford;

import javafx.scene.canvas.GraphicsContext;

public interface Renderer<T> {

	public void render(GraphicsContext gc, T model);
}
