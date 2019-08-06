package uk.ac.manchester.trafford.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConvertTest {

	@Test
	public void testMetersToCentimeters() {
		assertEquals(10000, Convert.metersToCentimeters(100));
		assertEquals(120, Convert.metersToCentimeters(1.2));
		assertEquals(133, Convert.metersToCentimeters(1.333));
		assertEquals(167, Convert.metersToCentimeters(1.666));
	}

	@Test
	public void testCentimetersToMeters() {
		assertEquals(100., Convert.centimetersToMeters(10000), 0.0005);
		assertEquals(0.01, Convert.centimetersToMeters(1), 0.0005);
	}

}
