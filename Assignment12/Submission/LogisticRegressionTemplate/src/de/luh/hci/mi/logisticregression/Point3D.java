package de.luh.hci.mi.logisticregression;

public class Point3D {
	
	public double x, y, z;
	
	public Point3D(double ax, double ay, double az) {
		x = ax;
		y = ay;
		z = az;
	}
	
	public Point3D(Point3D p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
