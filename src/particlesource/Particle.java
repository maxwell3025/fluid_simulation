package particlesource;

import vectors.Point2D;

public class Particle {
	Point2D pos;
	Point2D vel;

	public Particle(Point2D position, Point2D velocity) {
		pos = position;
		vel = velocity;
	}

	public void update(double time) {
		pos = Point2D.add(pos, vel.scale(time));
	}

}
