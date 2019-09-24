package particlesource;

import vectors.Point2D;

public class FluidParticle extends Particle {
	double pressure=0;
	double density;
	Point2D velchange = Point2D.Origin();

	public FluidParticle(Point2D position, Point2D velocity, double size) {
		super(position, velocity);
		density = size;
	}
	@Override
	public void update(double time) {
		pos = Point2D.add(pos, vel.scale(time));
		vel = Point2D.add(vel, velchange.scale(time));
		velchange = Point2D.Origin();
	}

}
