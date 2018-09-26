package uk.ac.manchester.trafford.util;

public class Convert {

	/**
	 * Convert meters to millimeters.
	 * 
	 * @param millimeters
	 * @return
	 */
	public static int metersToMillimeters(double meters) {
		return (int) Math.round(meters * 1000);
	}

	/**
	 * Convert millimeters to meters.
	 * 
	 * @param millimeters
	 * @return
	 */
	public static double millimetersToMeters(int millimeters) {
		return millimeters / 1000.;
	}

	/**
	 * Convert kilometers per hour to meters per second.
	 * 
	 * @param kmph
	 * @return
	 */
	public static double kmphToMps(double kmph) {
		return kmph / 3.6;
	}

	/**
	 * Convert meters per second to kilometers per hour.
	 * 
	 * @param mps
	 * @return
	 */
	public static double mpsToKmph(double mps) {
		return mps * 3.6;
	}

	/**
	 * Convert kilometers per hour to millimeters per second.
	 * 
	 * @param kmph
	 * @return
	 */
	public static int kmphToMmps(double kmph) {
		return metersToMillimeters(kmphToMps(kmph));
	}

	/**
	 * Convert millimeters per second to kilometers per hour.
	 * 
	 * @param mmps
	 * @return
	 */
	public static double mmpsToKmph(int mmps) {
		return mpsToKmph(millimetersToMeters(mmps));
	}
}
