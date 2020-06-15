package sick;

public class Point {

	private double X;
	private double Y;
	private double D;
	private double A;
	
	//CONSTRUCTOR
	
	public Point(int dis, int ang) {
		
		//SAVE VALUES OF DISTANCE AND ANGLE IN DEGREES
		this.D = (double) dis;
		this.A = ((double) ang) / 10000;
		
		//CALCULATE ANGLE VALUE IN RADIANS
		double angrad = Math.toRadians(this.A);
		
		//TRANSFORM FROM POLAR COORDINATE SYSTEM TO CARTESIAN
		this.X = dis * Math.cos(angrad);
		this.Y = dis * Math.sin(angrad);
		
	}
	
	//INFO
	//PRINTS INFORMATION ABOUT POINT
	
	public void info() {
		System.out.print("POINT");
		System.out.print("POLAR: (" + this.D + "," + this.A + ")");
		System.out.print("CARTESIAN: (" + this.X + "," + this.Y + ")");
	}
	
	//GET
	
	public double getD() {
		return this.D;
	}
	
	public double getA() {
		return this.A;
	}
	
	public double getX() {
		return this.X;
	}
	
	public double getY() {
		return this.Y;
	}
	
}
