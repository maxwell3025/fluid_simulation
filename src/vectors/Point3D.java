package vectors;

public class Point3D {
	public double x;
	public double y;
	public double z;
	public static final Point3D Origin(){
		return new Point3D(0, 0, 0);
	}

	public Point3D copy(){
		return new Point3D(x,y,z);
	}
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static double dotProduct(Point3D a, Point3D b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static Point3D add(Point3D a, Point3D b) {
		return new Point3D(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public Point3D scale(double scale) {
		return new Point3D(x * scale, y * scale, z * scale);
	}

	public double dist() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public boolean equals(Object Obj) {
		if (Obj.getClass() == Point3D.class) {
			Point3D obj = (Point3D) Obj;
			return (x == obj.x) && (y == obj.y) && (z == obj.z);
		}
		return false;
	}

}
