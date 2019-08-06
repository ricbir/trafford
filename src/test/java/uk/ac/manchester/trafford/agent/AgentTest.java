package uk.ac.manchester.trafford.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.Constants;
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
	@Mock
	private IDMAccelerator accelerator;
	@Mock
	private LeaderFinder finder;

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
	private Agent leader;

	@BeforeEach
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
		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.setRoutingAlgorithm(routingAlgorithm);

		verify(routingAlgorithm).getPath(startSegment, endSegment);
		assertSame(startPosition, agent.getPosition());
		assertFalse(agent.hasArrived());
	}

	@Test
	public void testPositionChange() throws DistanceOutOfBoundsException {
		when(startPosition.add(anyDouble(), any())).thenReturn(otherPosition);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(startPosition).add(anyDouble(), eq(segmentList.subList(1, segmentList.size())));
		assertSame(otherPosition, agent.getPosition());
	}

	@Test
	public void testFindLeader() throws DistanceOutOfBoundsException {
		leader = new Agent(network, otherPosition, destination, accelerator, routingAlgorithm, finder);

		when(finder.findLeader(anyList(), anyDouble(), anyDouble())).thenReturn(leader);
		when(startPosition.getDistance()).thenReturn(50.);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(finder).findLeader(segmentList, 50, 50 + Constants.LOOK_AHEAD_DISTANCE);
	}

	@Test
	public void testMoveDestinationFar() throws DistanceOutOfBoundsException {
		when(startPosition.distanceTo(eq(destination), any())).thenReturn(200.);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(accelerator).getAcceleration(0, 30);
	}

	@Test
	public void testMoveDestinationClose() throws DistanceOutOfBoundsException {
		when(startPosition.distanceTo(eq(destination), any())).thenReturn(30.);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(accelerator).getAcceleration(0, 30, 30 + Constants.MINIMUM_SPACING, 0);
	}

	@Test
	public void testMoveLeaderFar() throws DistanceOutOfBoundsException {
		leader = new Agent(network, otherPosition, destination, accelerator, routingAlgorithm, finder);

		when(finder.findLeader(anyList(), anyDouble(), anyDouble())).thenReturn(leader);
		when(startPosition.distanceTo(eq(destination), any())).thenReturn(Constants.LOOK_AHEAD_DISTANCE);
		when(startPosition.distanceTo(eq(otherPosition), any())).thenReturn(Constants.LOOK_AHEAD_DISTANCE);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(accelerator).getAcceleration(0, 30);
	}

	@Test
	public void testMoveLeaderClose() throws DistanceOutOfBoundsException {
		leader = new Agent(network, otherPosition, destination, accelerator, routingAlgorithm, finder);

		when(finder.findLeader(anyList(), anyDouble(), anyDouble())).thenReturn(leader);
		when(startPosition.distanceTo(eq(destination), any())).thenReturn(Constants.LOOK_AHEAD_DISTANCE);
		when(startPosition.distanceTo(eq(otherPosition), any())).thenReturn(30.);

		agent = new Agent(network, startPosition, destination, accelerator, routingAlgorithm, finder);
		agent.update();

		verify(accelerator).getAcceleration(0, 30, 30, 0);
	}

	@Test
	public void testHasArrived() {
		agent = new Agent(network, otherPosition, destination, accelerator, routingAlgorithm, finder);

		when(otherPosition.getSegment()).thenReturn(endSegment);
		when(otherPosition.getDistance()).thenReturn(SEGMENT_LENGTH);

		assertTrue(agent.hasArrived());
	}

}
