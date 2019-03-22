package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.Constants;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.EdgeAccessController.State;

public class TimedTrafficLightTest {

	TimedTrafficLight trafficLight;
	private static final int GREEN_SECONDS = 5;
	private static final int YELLOW_SECONDS = 3;

	@Mock
	private RoadNetwork network;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTimedTrafficLight() {
		trafficLight = new TimedTrafficLight(GREEN_SECONDS, YELLOW_SECONDS, 2, network);

		assertEquals(trafficLight.stages(), 2);
		assertTrue(trafficLight.getController(0) instanceof EdgeAccessController);
		assertTrue(trafficLight.getController(1) instanceof EdgeAccessController);
		assertNotSame(trafficLight.getController(0), trafficLight.getController(1));

		try {
			assertNull(trafficLight.getController(2));
			fail("IndexOutOfBoundsException expected");
		} catch (IndexOutOfBoundsException e) {

		}

		verify(network).addTrafficLight(trafficLight);
	}

	@Test
	public void testSequence() {
		trafficLight = new TimedTrafficLight(GREEN_SECONDS, YELLOW_SECONDS, 3, network);

		for (int i = 0; i < GREEN_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertGreen(trafficLight.getController(0));
			assertRed(trafficLight.getController(1));
			assertRed(trafficLight.getController(2));
		}

		for (int i = 0; i < YELLOW_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertYellow(trafficLight.getController(0));
			assertRed(trafficLight.getController(1));
			assertRed(trafficLight.getController(2));
		}

		for (int i = 0; i < GREEN_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertRed(trafficLight.getController(0));
			assertGreen(trafficLight.getController(1));
			assertRed(trafficLight.getController(2));
		}

		for (int i = 0; i < YELLOW_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertRed(trafficLight.getController(0));
			assertYellow(trafficLight.getController(1));
			assertRed(trafficLight.getController(2));
		}

		for (int i = 0; i < GREEN_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertRed(trafficLight.getController(0));
			assertRed(trafficLight.getController(1));
			assertGreen(trafficLight.getController(2));
		}

		for (int i = 0; i < YELLOW_SECONDS * Constants.UPDATES_PER_SECOND; i++) {
			trafficLight.update();

			assertRed(trafficLight.getController(0));
			assertRed(trafficLight.getController(1));
			assertYellow(trafficLight.getController(2));
		}

		trafficLight.update();

		assertGreen(trafficLight.getController(0));
		assertRed(trafficLight.getController(1));
		assertRed(trafficLight.getController(2));
	}

	private void assertRed(EdgeAccessController controller) {
		assertEquals(State.TL_RED, controller.getState());
	}

	private void assertGreen(EdgeAccessController controller) {
		assertEquals(State.TL_GREEN, controller.getState());
	}

	private void assertYellow(EdgeAccessController controller) {
		assertEquals(State.TL_YELLOW, controller.getState());
	}

}
