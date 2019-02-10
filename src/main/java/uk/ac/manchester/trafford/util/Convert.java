package uk.ac.manchester.trafford.util;

public class Convert {

	/**
	 * Convert meters to centimeters.
	 * 
	 * @param meters
	 * @return
	 */
	public static int metersToCentimeters(double meters) {
		return (int) Math.round(meters * 100);
	}

	/**
	 * Convert centimeters to meters.
	 * 
	 * @param centimeters
	 * @return
	 */
	public static double centimetersToMeters(int centimeters) {
		return centimetersToMeters((double) centimeters);
	}

	public static double centimetersToMeters(double centimeters) {
		return centimeters / 100.;
	}
}
