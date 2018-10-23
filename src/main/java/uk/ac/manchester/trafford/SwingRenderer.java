package uk.ac.manchester.trafford;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.ac.manchester.trafford.agent.Agent;
import uk.ac.manchester.trafford.network.Point;
import uk.ac.manchester.trafford.network.RoadNetwork;
import uk.ac.manchester.trafford.network.edge.Edge;

@SuppressWarnings("serial")
public class SwingRenderer extends JFrame implements Renderer {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(SwingRenderer.class.getName());

	private SimulationPanel simulationPanel = new SimulationPanel();

	private RoadNetwork network;

	public SwingRenderer() {
		super("Test");

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		// p.add(startButton);
		// p.add(pauseButton);
		// p.add(quitButton);
		cp.add(simulationPanel, BorderLayout.CENTER);
		cp.add(p, BorderLayout.SOUTH);
		setSize(900, 900);

		// startButton.addActionListener(this);
		// quitButton.addActionListener(this);
		// pauseButton.addActionListener(this);
	}

	@Override
	public void render(double interpolation) {
		simulationPanel.setInterpolation(interpolation);
		simulationPanel.repaint();
	}

	@Override
	public void setNetwork(RoadNetwork network) {
		this.network = network;
	}

	private class SimulationPanel extends JPanel {

		// private double interpolation;

		public SimulationPanel() {

		}

		public void setInterpolation(double interpolation2) {
			// interpolation = interpolation2;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			for (Edge edge : network.edgeSet()) {
				Point source = network.getEdgeSource(edge);
				Point target = network.getEdgeTarget(edge);

				g.setColor(Color.GRAY);
				g.drawLine(scale(source.getX()), scale(source.getY()), scale(target.getX()), scale(target.getY()));

				switch (edge.getAccessState()) {
				case RED:
					g.setColor(Color.RED);
					break;
				case YELLOW:
					g.setColor(Color.YELLOW);
					break;
				case GREEN:
					g.setColor(Color.GREEN);
					break;
				}
				g.drawOval(scale(source.getX()) - 2, scale(source.getY()) - 2, 4, 4);
				g.fillOval(scale(source.getX()) - 2, scale(source.getY()) - 2, 4, 4);
			}

			Agent[] agentSetSnapshot = network.agentSetSnapshot();

			g.setColor(Color.BLACK);
			for (Agent agent : agentSetSnapshot) {
				Point point = network.getCoordinates(agent);
				if (point != null) {
					g.drawRect(scale(point.getX()) - 1, scale(point.getY()) - 1, 2, 2);
					g.fillRect(scale(point.getX()) - 1, scale(point.getY()) - 1, 2, 2);
				}
			}

			g.setColor(Color.BLACK);
			g.drawString("" + network.getAverageCongestion(), 20, 800);
		}

		private int scale(int position) {
			return (int) Math.round(position * Constants.RENDER_SCALING_FACTOR);
		}
	}
}
