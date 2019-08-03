package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class IDMAcceleratorTest {

	private IDMAccelerator accelerator;

	@Before
	public void setUp() throws Exception {
		accelerator = new IDMAccelerator(1, 1, 1, 1);
	}

	@Test
	public void testAccelerationNoObstacle() {
		assertEquals(1, accelerator.getAcceleration(0, 30), 0.1);
	}

	@Test
	public void testAccelerationObstacleSameSpeed() {
		assertEquals(0, accelerator.getAcceleration(10, 30, 11, 10), 0.1);
	}

	@Test
	public void testAccelerationFasterObstacle() {
		assertEquals(1, accelerator.getAcceleration(0, 30, 10, 30), 0.1);
	}

	@Test
	public void testAccelerationSlowerObstacle() {
		assertTrue(accelerator.getAcceleration(30, 30, 10, 10) < 0);
	}

}
