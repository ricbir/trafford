package uk.ac.manchester.trafford.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SegmentTest {

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void testEquals() {
		Segment segment = new Segment(Point.create(10, 10), Point.create(20, 20));
		Segment sameSegment = segment;
		Segment equalSegment = new Segment(Point.create(10, 10), Point.create(20, 20));
		Segment differentSegmentA = new Segment(Point.create(15, 15), Point.create(25, 25));
		Segment differentSegmentB = new Segment(Point.create(10, 10), Point.create(25, 25));
		Segment differentSegmentC = new Segment(Point.create(15, 15), Point.create(20, 20));

		assertTrue(segment.equals(sameSegment));
		assertTrue(sameSegment.equals(segment));

		assertTrue(segment.equals(equalSegment));
		assertTrue(equalSegment.equals(segment));

		assertFalse(segment.equals(differentSegmentA));
		assertFalse(differentSegmentA.equals(segment));

		assertFalse(segment.equals(differentSegmentB));
		assertFalse(differentSegmentB.equals(segment));

		assertFalse(segment.equals(differentSegmentC));
		assertFalse(differentSegmentC.equals(segment));
	}

	@Test
	public void testGetCoordinates() {
		Segment segment = new Segment(Point.create(10, 10), Point.create(50, 20));

		Point point = Point.create(30, 15);

		assertEquals(point.getX(), segment.getPointX(segment.getLength() / 2), 0.0001);
		assertEquals(point.getY(), segment.getPointY(segment.getLength() / 2), 0.0001);
	}

}
