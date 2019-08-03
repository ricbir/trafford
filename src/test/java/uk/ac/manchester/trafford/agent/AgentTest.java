package uk.ac.manchester.trafford.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;
import uk.ac.manchester.trafford.network.SegmentConnection;

public class AgentTest {

	private static final double SEGMENT_LENGTH = 100;

	@Mock
	private RoadNetwork network;
	@Mock
	private ShortestPathAlgorithm<Segment, SegmentConnection> routingAlgorithm;
	@Mock
	private GraphPath<Segment, SegmentConnection> path;

	private Segment startSegment = new Segment(Point.create(0, 0), Point.create(SEGMENT_LENGTH, 0));
	private Segment middleSegment = new Segment(Point.create(SEGMENT_LENGTH, 0), Point.create(SEGMENT_LENGTH * 2, 0));
	private Segment endSegment = new Segment(Point.create(SEGMENT_LENGTH * 2, 0), Point.create(SEGMENT_LENGTH * 3, 0));

	@Mock
	private Position startPosition;
	@Mock
	private Position destination;
	@Mock
	private Position otherPosition;

	private List<Segment> segmentList = new ArrayList<>(3);

	private Agent agent;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(startPosition.getSegment()).thenReturn(startSegment);
		when(destination.getSegment()).thenReturn(endSegment);

		segmentList.add(startSegment);
		segmentList.add(middleSegment);
		segmentList.add(endSegment);

		when(path.getVertexList()).thenReturn(segmentList);
		when(path.getStartVertex()).thenReturn(startSegment);
		when(routingAlgorithm.getPath(startSegment, endSegment)).thenReturn(path);
	}

	@Test
	public void testInitialize() {
		agent = new Agent(network, routingAlgorithm, startPosition, destination);
		agent.setRoutingAlgorithm(routingAlgorithm);

		verify(routingAlgorithm).getPath(startSegment, endSegment);
		assertSame(startPosition, agent.getPosition());
		assertFalse(agent.hasArrived());
	}

	@Test
	public void testMove() throws DistanceOutOfBoundsException {
		when(startPosition.add(anyDouble(), any())).thenReturn(otherPosition);
		when(otherPosition.getSegment()).thenReturn(middleSegment);

		agent = new Agent(network, routingAlgorithm, startPosition, destination);
		agent.update();

		verify(startPosition).add(anyDouble(), eq(segmentList.subList(1, segmentList.size())));
		assertSame(otherPosition, agent.getPosition());
		verify(routingAlgorithm).getPath(middleSegment, endSegment);
		assertFalse(agent.hasArrived());
	}

	@Test
	public void testHasArrived() {
		agent = new Agent(network, routingAlgorithm, otherPosition, destination);

		when(otherPosition.getSegment()).thenReturn(endSegment);
		when(otherPosition.getDistance()).thenReturn(SEGMENT_LENGTH);

		assertTrue(agent.hasArrived());
	}

}
