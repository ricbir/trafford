package uk.ac.manchester.trafford.agent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.manchester.trafford.exceptions.DistanceOutOfBoundsException;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.Segment;

public class LeaderFinderTest {

	private static final double SEGMENT_LENGTH = 100;

	@Mock
	private RoadNetwork network;

	@Mock
	private Agent agent;

	private Segment firstSegment;
	private Segment secondSegment;
	private Segment thirdSegment;

	private List<Segment> segments;

	private LeaderFinder finder;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		firstSegment = new Segment(Point.create(0, 0), Point.create(SEGMENT_LENGTH, 0));
		secondSegment = new Segment(Point.create(SEGMENT_LENGTH, 0), Point.create(SEGMENT_LENGTH * 2, 0));
		thirdSegment = new Segment(Point.create(SEGMENT_LENGTH * 2, 0), Point.create(SEGMENT_LENGTH * 3, 0));
		segments = Arrays.asList(firstSegment, secondSegment, thirdSegment);
	}

	@Test
	public void testFindLeaderEmpty() {
		when(network.getAgentsOnSegment(firstSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(secondSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(thirdSegment)).thenReturn(Arrays.asList());

		finder = new LeaderFinder(network);

		assertNull(finder.findLeader(segments, 0, SEGMENT_LENGTH * 2));
		verify(network).getAgentsOnSegment(firstSegment);
		verify(network).getAgentsOnSegment(secondSegment);
		verify(network, never()).getAgentsOnSegment(thirdSegment);
	}

	@Test
	public void testFindLeaderFirstSegment() throws DistanceOutOfBoundsException {
		when(network.getAgentsOnSegment(firstSegment)).thenReturn(Arrays.asList(agent));
		when(network.getAgentsOnSegment(secondSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(thirdSegment)).thenReturn(Arrays.asList());
		when(agent.getPosition()).thenReturn(Position.create(firstSegment, 50));

		finder = new LeaderFinder(network);

		assertSame(agent, finder.findLeader(segments, 0, SEGMENT_LENGTH * 2));
		verify(network).getAgentsOnSegment(firstSegment);
		verify(network, never()).getAgentsOnSegment(secondSegment);
		verify(network, never()).getAgentsOnSegment(thirdSegment);
	}

	@Test
	public void testFindLeaderSecondSegment() throws DistanceOutOfBoundsException {
		when(network.getAgentsOnSegment(firstSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(secondSegment)).thenReturn(Arrays.asList(agent));
		when(network.getAgentsOnSegment(thirdSegment)).thenReturn(Arrays.asList());
		when(agent.getPosition()).thenReturn(Position.create(secondSegment, 50));

		finder = new LeaderFinder(network);

		assertSame(agent, finder.findLeader(segments, 0, SEGMENT_LENGTH * 2));
		verify(network).getAgentsOnSegment(firstSegment);
		verify(network).getAgentsOnSegment(secondSegment);
		verify(network, never()).getAgentsOnSegment(thirdSegment);
	}

	@Test
	public void testFindLeaderTooFar() throws DistanceOutOfBoundsException {
		when(network.getAgentsOnSegment(firstSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(secondSegment)).thenReturn(Arrays.asList());
		when(network.getAgentsOnSegment(thirdSegment)).thenReturn(Arrays.asList(agent));
		when(agent.getPosition()).thenReturn(Position.create(thirdSegment, 60));

		finder = new LeaderFinder(network);

		assertNull(finder.findLeader(segments, 0, SEGMENT_LENGTH * 2.5));
		verify(network).getAgentsOnSegment(firstSegment);
		verify(network).getAgentsOnSegment(secondSegment);
		verify(network).getAgentsOnSegment(thirdSegment);
	}

}
