package gridsource;

import vectors.Point2D;

public class Cell {
	double pres;
	Point2D vel = Point2D.Origin();
	public Cell(double pressure) {
		pres=pressure;
	}

}
