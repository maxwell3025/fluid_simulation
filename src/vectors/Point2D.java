package vectors;

public class Point2D {
	public double x;
	public double y;
	public static final Point2D Origin(){
		return new Point2D(0, 0);
	}
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Point2D copy(){
		return new Point2D(x,y);
	}
	public static double dotProduct(Point2D a, Point2D b) {
		return a.x * b.x + a.y * b.y;
	}

	public static Point2D add(Point2D a, Point2D b) {
		return new Point2D(a.x + b.x, a.y + b.y);
	}

	public Point2D scale(double scale) {
		return new Point2D(x * scale, y * scale);
	}

	public double dist() {
		return Math.sqrt(x * x + y * y);
	}

	public double angle() {
		return Math.atan2(x,y);
	}

	@Override
	public boolean equals(Object Obj) {
		if (Obj.getClass() == Point2D.class) {
			Point2D obj = (Point2D) Obj;
			return x == obj.x && y == obj.y;
		}
		return false;
	}
	public static Point2D minx(Point2D a,Point2D b){
		if(a.x<b.x){
			return a;
		}
		else{
			return b;
		}
	}public static Point2D maxx(Point2D a,Point2D b){
		if(b.x<a.x){
			return a;
		}
		else{
			return b;
		}
	}
	public static Point2D miny(Point2D a,Point2D b){
		if(a.y<b.y){
			return a;
		}
		else{
			return b;
		}
	}public static Point2D maxy(Point2D a,Point2D b){
		if(b.y<a.y){
			return a;
		}
		else{
			return b;
		}
	}

}
