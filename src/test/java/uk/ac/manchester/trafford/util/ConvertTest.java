package uk.ac.manchester.trafford.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.manchester.trafford.util.Convert;

public class ConvertTest {

	@Test
	public void testMetersToMillimeters() {
		assertEquals(100000, Convert.metersToMillimeters(100));
		assertEquals(1200, Convert.metersToMillimeters(1.2));
		assertEquals(1333, Convert.metersToMillimeters(1.3333));
		assertEquals(1667, Convert.metersToMillimeters(1.6666));
	}

	@Test
	public void testMillimetersToMeters() {
		assertEquals(100., Convert.millimetersToMeters(100000), 0.0005);
		assertEquals(0.001, Convert.millimetersToMeters(1), 0.0005);
	}

	@Test
	public void testKmphToMps() {
		assertEquals(10., Convert.kmphToMps(36), 0.0005);
	}

	@Test
	public void testMpsToKmph() {
		assertEquals(36., Convert.mpsToKmph(10), 0.0005);
	}

}
