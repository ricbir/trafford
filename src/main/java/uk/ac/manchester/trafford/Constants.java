package uk.ac.manchester.trafford;

public class Constants {
	public static final int UPDATES_PER_SECOND = 30;
	public static final int RENDERS_PER_SECOND = 30;

	public static final double SPATIAL_SENSITIVITY = 0.0005;

	/** Agent acceleration in m/s^2 */
	public static final double AGENT_ACCELERATION = 4;
	/** Agent deceleration in m/s^2 */
	public static final double AGENT_DECELERATION = 2;
	/** One second, in nanoseconds */
	public static final int NANOSECONDS_PER_SECOND = 1_000_000_000;

	/** Minimum space between agents, in meters */
	public static final double MINIMUM_SPACING = 0.5;
	/**
	 * Time headway (i.e. the time it would take an agent to reach its leader)
	 * between agents, in seconds
	 */
	public static final double DESIRED_TIME_HEADWAY = 1;

	public static final double LANE_WIDTH = 3;

	public static final double RENDER_SCALING_FACTOR = 0.1;

}
