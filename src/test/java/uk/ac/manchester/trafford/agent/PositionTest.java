package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.Segment;

public class PositionTest {

	private static final double SEGMENT_LENGTH = 100;

	@Mock
	private Segment firstSegment;

	@Mock
	private Segment secondSegment;

	@Mock
	private Segment thirdSegment;

	private List<Segment> followingSegments;

	private StringWriter sw = new StringWriter();
	private PrintWriter pw = new PrintWriter(sw);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(firstSegment.getLength()).thenReturn(SEGMENT_LENGTH);
		when(secondSegment.getLength()).thenReturn(SEGMENT_LENGTH);
		when(thirdSegment.getLength()).thenReturn(SEGMENT_LENGTH);

		followingSegments = Arrays.asList(secondSegment, thirdSegment);
	}

	@Test
	public void testCreateSegmentStart() {
		try {
			Position.create(firstSegment, 0);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}
	}

	@Test
	public void testCreateSegmentMiddle() {
		try {
			Position.create(firstSegment, SEGMENT_LENGTH / 2);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}
	}

	@Test
	public void testCreateSegmentEnd() {
		try {
			Position.create(firstSegment, SEGMENT_LENGTH);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}
	}

	@Test
	public void testCreateNegativeDistance() {
		try {
			Position.create(firstSegment, -1);
			fail("Expected exception");
		} catch (DistanceOutOfBoundsException e) {

		}
	}

	@Test
	public void testCreateDistanceTooLong() {
		try {
			Position.create(firstSegment, SEGMENT_LENGTH + 1);
			fail("Expected exception");
		} catch (DistanceOutOfBoundsException e) {

		}
	}

	@Test
	public void testPositionCoordinates() throws DistanceOutOfBoundsException {
		double distance = SEGMENT_LENGTH / 2;
		Position position = Position.create(firstSegment, distance);

		when(firstSegment.getPointX(distance)).thenReturn(5.);
		when(firstSegment.getPointY(distance)).thenReturn(10.);

		assertEquals(5, position.getX(), 0.0001);
		verify(firstSegment).getPointX(distance);
		assertEquals(10, position.getY(), 0.0001);
		verify(firstSegment).getPointY(distance);
	}

	@Test
	public void testAddWithinBounds() throws DistanceOutOfBoundsException {
		Position position = Position.create(firstSegment, 0);

		double delta = SEGMENT_LENGTH / 2;

		try {
			position = position.add(delta, followingSegments);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}

		assertEquals(firstSegment, position.getSegment());
		assertEquals(delta, position.getDistance(), 0.0001);

	}

	@Test
	public void testAddToSecondSegment() throws DistanceOutOfBoundsException {
		Position position = Position.create(firstSegment, 0);

		double delta = SEGMENT_LENGTH + SEGMENT_LENGTH / 2;

		try {
			position = position.add(delta, followingSegments);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}

		assertEquals(secondSegment, position.getSegment());
		assertEquals(delta - firstSegment.getLength(), position.getDistance(), 0.0001);
	}

	@Test
	public void testAddToThirdSegment() throws DistanceOutOfBoundsException {
		Position position = Position.create(firstSegment, 0);

		double delta = SEGMENT_LENGTH * 2 + SEGMENT_LENGTH / 2;

		try {
			position = position.add(delta, followingSegments);
		} catch (DistanceOutOfBoundsException e) {
			exceptionFail(e);
		}

		assertEquals(thirdSegment, position.getSegment());
		assertEquals(delta - firstSegment.getLength() - secondSegment.getLength(), position.getDistance(), 0.0001);
	}

	@Test
	public void testAddOutOfBoundsSegment() throws DistanceOutOfBoundsException {
		Position position = Position.create(firstSegment, 0);

		double delta = SEGMENT_LENGTH * 3 + SEGMENT_LENGTH / 2;

		try {
			position = position.add(delta, followingSegments);
			fail();
		} catch (DistanceOutOfBoundsException e) {

		}

	}

	@Test
	public void testCompareTo() throws DistanceOutOfBoundsException {
		Position position1 = Position.create(firstSegment, 0);
		Position position2 = Position.create(firstSegment, 10);
		Position position3 = Position.create(secondSegment, 0);

		assertTrue(position1.compareTo(position2) < 0);
		assertTrue(position2.compareTo(position1) > 0);
		assertTrue(position1.compareTo(position1) == 0);

		try {
			position1.compareTo(position3);
			fail();
		} catch (IllegalArgumentException e) {

		}
	}

	private void exceptionFail(Exception e) {
		e.printStackTrace(pw);
		fail(sw.toString());
	}

}
