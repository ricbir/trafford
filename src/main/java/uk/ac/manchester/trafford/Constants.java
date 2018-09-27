package uk.ac.manchester.trafford;

import uk.ac.manchester.trafford.util.Convert;

public class Constants {
	public static final int TICKS_PER_SECOND = 50;

	public static final double SPATIAL_SENSITIVITY = 0.0005;

	public static final double AGENT_DISTANCE = 1;
	/** Agent acceleration in mm/s^2 */
	public static final int AGENT_ACCELERATION_MMS2 = 4000;
	/** Agent deceleration in mm/s^2 */
	public static final int AGENT_DECELERATION_MMS2 = 7000;
	/** Agent maximum speed in km/h */
	private static final int AGENT_MAX_SPEED_KMH = 50;
	/** Agent maximum speed in mm/s */
	public static final int AGENT_MAX_SPEED_MMS = Convert.metersToMillimeters(Convert.kmphToMps(AGENT_MAX_SPEED_KMH));

}
