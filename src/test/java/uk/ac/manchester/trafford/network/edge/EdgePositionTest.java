package uk.ac.manchester.trafford.network.edge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EdgePositionTest {

	private EdgePosition position1;
	private EdgePosition position2;

	@Mock
	private Edge edge1;

	@Mock
	private Edge edge2;

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
	public void testEquals() {
		position1 = new EdgePosition(edge1, 10);
		position2 = new EdgePosition(edge1, 10);

		assertTrue(position1.equals(position2));
		assertTrue(position2.equals(position1));

		position1 = new EdgePosition(edge1, 10);
		position2 = new EdgePosition(edge2, 10);

		assertFalse(position1.equals(position2));
		assertFalse(position2.equals(position1));

		position1 = new EdgePosition(edge1, 10);
		position2 = new EdgePosition(edge1, 20);

		assertFalse(position1.equals(position2));
		assertFalse(position2.equals(position1));

		position1 = new EdgePosition(edge1, 10);
		position2 = new EdgePosition(edge2, 20);

		assertFalse(position1.equals(position2));
		assertFalse(position2.equals(position1));
	}
}
