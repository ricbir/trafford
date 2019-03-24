package uk.ac.manchester.trafford.pojo;

public class TrafficLightPojo {
	public int id;
	public int greenSeconds;
	public int yellowSeconds;

	public TrafficLightPojo() {
	}

	public TrafficLightPojo(int id, int greenSeconds, int yellowSeconds) {
		this.id = id;
		this.greenSeconds = greenSeconds;
		this.yellowSeconds = yellowSeconds;
	}
}
