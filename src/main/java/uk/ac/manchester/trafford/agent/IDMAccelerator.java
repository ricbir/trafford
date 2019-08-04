package uk.ac.manchester.trafford.agent;

import uk.ac.manchester.trafford.Constants;

public class IDMAccelerator {

	private static final double DELTA_COEFFICIENT = 4;

	private double accelerationCoefficient;
	private double decelerationCoefficient;
	private double desiredTimeHeadway;
	private double minimumSpacing;

	public IDMAccelerator(double accelerationCoefficient, double decelerationCoefficient, double desiredTimeHeadway,
			double minimumSpacing) {
		this.accelerationCoefficient = accelerationCoefficient;
		this.decelerationCoefficient = decelerationCoefficient;
		this.desiredTimeHeadway = desiredTimeHeadway;
		this.minimumSpacing = minimumSpacing;
	}

	public IDMAccelerator() {
		this(Constants.AGENT_ACCELERATION, Constants.AGENT_DECELERATION, Constants.DESIRED_TIME_HEADWAY,
				Constants.MINIMUM_SPACING);
	}

	/**
	 * Calculates the acceleration for an agent using the Intelligent Driver Model
	 * (https://en.wikipedia.org/wiki/Intelligent_driver_model).
	 * 
	 * @param speed            The speed of the agent
	 * @param targetSpeed      The target speed for the agent
	 * @param obstacleSpeed    The speed of the obstacle
	 * @param obstacleDistance Distance between the agent and the obstacle
	 * @return The acceleration
	 */
	double getAcceleration(double speed, double targetSpeed, double obstacleDistance, double obstacleSpeed) {
		double freeRoadTerm = getFreeRoadTerm(speed, targetSpeed);
		double interactionTerm = getInteractionTerm(speed, obstacleDistance, obstacleSpeed);

		return accelerationCoefficient * (1 - freeRoadTerm - interactionTerm);
	}

	/**
	 * Calculates the speed delta for an agent using the Intelligent Driver Model
	 * (https://en.wikipedia.org/wiki/Intelligent_driver_model).
	 * 
	 * @param speed            The speed of the agent
	 * @param targetSpeed      The target speed for the agent
	 * @param obstacleSpeed    The speed of the obstacle
	 * @param obstacleDistance Distance between the agent and the obstacle
	 * @return The acceleration
	 */
	double getAcceleration(double speed, double targetSpeed) {
		double freeRoadTerm = getFreeRoadTerm(speed, targetSpeed);

		return accelerationCoefficient * (1 - freeRoadTerm);
	}

	private double getFreeRoadTerm(double speed, double targetSpeed) {
		return Math.pow(speed / targetSpeed, DELTA_COEFFICIENT);
	}

	private double getInteractionTerm(double speed, double obstacleDistance, double obstacleSpeed) {
		double obstacleSpeedDelta = speed - obstacleSpeed;
		double denominator = 2 * Math.sqrt(accelerationCoefficient * decelerationCoefficient);

		return Math.pow((minimumSpacing + speed * desiredTimeHeadway + (speed * obstacleSpeedDelta) / denominator)
				/ obstacleDistance, 2);
	}
}
