package uk.ac.manchester.trafford;

public interface Renderer<T extends Model> {
	public void render();

	public void setModel(T model);

	public T getModel();

	public void setScalingFactor(double scalingFactor);

	public double getScalingFactor();

	public void setTranslateX(double translateX);

	public double getTranslateX();

	public void setTranslateY(double translateY);

	public double getTranslateY();
}
