package uk.ac.manchester.trafford.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.agent.IDMAccelerator;
import uk.ac.manchester.trafford.agent.LeaderFinder;
import uk.ac.manchester.trafford.agent.Position;

@SuppressWarnings("serial")
public class RoadNetwork extends DefaultDirectedGraph<Segment, SegmentConnection> {

	private static final Random RANDOM = new Random();

	private ShortestPathAlgorithm<Segment, SegmentConnection> shortestPathAlgorithm;
	private LeaderFinder leaderFinder;
	private Comparator<Agent> agentComparator;

	private ConcurrentMap<Segment, List<Agent>> agentsBySegment;

	private List<Position> agentPositions;

	public RoadNetwork() {
		super(SegmentConnection.class);
		leaderFinder = new LeaderFinder(this);
		shortestPathAlgorithm = new DijkstraShortestPath<Segment, SegmentConnection>(this);
		agentComparator = new Comparator<Agent>() {

			@Override
			public int compare(Agent agent0, Agent agent1) {
				return agent0.getPosition().compareTo(agent1.getPosition());
			}

		};

		agentsBySegment = new ConcurrentHashMap<>();
	}

	public void update() {
		agentPositions = new ArrayList<>();
		for (Entry<Segment, List<Agent>> entry : agentsBySegment.entrySet()) {
			for (Agent agent : entry.getValue()) {
				agent.update();
				agentPositions.add(agent.getPosition());

				if (!agent.getSegment().equals(entry.getKey())) {
					entry.getValue().remove(agent);
					agentsBySegment.get(agent.getSegment()).add(agent);
				}
			}
		}

		for (List<Agent> agentList : agentsBySegment.values()) {
			agentList.sort(agentComparator);
		}
	}

	public List<Agent> getAgentsOnSegment(Segment segment) {
		return Collections.unmodifiableList(agentsBySegment.get(segment));
	}

	public List<Position> getAgentPositions() {
		return null;
	}

	public Agent spawnAgent() {
		Segment origin = getRandomSegment();
		Segment destination = getRandomSegment();
		while (origin.equals(destination)) {
			destination = getRandomSegment();
		}

		Agent agent = new Agent(this, Position.random(origin), Position.random(destination), new IDMAccelerator(),
				shortestPathAlgorithm, leaderFinder);

		agentPositions.add(agent.getPosition());
		agentsBySegment.get(agent.getSegment()).add(agent);
		agentsBySegment.get(agent.getSegment()).sort(agentComparator);

		return agent;
	}

	private Segment getRandomSegment() {
		int item = RANDOM.nextInt(vertexSet().size());
		int i = 0;
		for (Segment segment : vertexSet()) {
			if (i == item)
				return segment;
			i++;
		}
		return null;
	}

	@Override
	public Segment addVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addVertex(Segment segment) {
		agentsBySegment.put(segment, new CopyOnWriteArrayList<>());
		return super.addVertex(segment);
	}
}
