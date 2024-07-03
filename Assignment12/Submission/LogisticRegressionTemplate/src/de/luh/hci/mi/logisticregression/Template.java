package de.luh.hci.mi.logisticregression;

import java.util.Vector;



public class Template {

	/** The sample points of this gesture trace. */
	private Vector<Point3D> samples;
	
	/** The gesture class that this template is an example of. */
	private int gestureId;

	public Template(int aId) {
		samples = new Vector<Point3D>();
		gestureId = aId;
	}

	public Template(Vector<Point3D> aVector, int aId) {
		samples = aVector;
		gestureId = aId;
	}
	
	public void add(double x, double y, double z) {
		samples.add(new Point3D(x,y,z));
	}
	
	public Point3D get(int i) {
		return samples.get(i);
	}
	
	public int size() {
		return samples.size();
	}
	
	public int getGestureId() {
		return gestureId;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[id = " + gestureId + ", vector = \n");
		if (samples.size() <= 0) {
			sb.append("(null)");
		} else {
			sb.append(samples.get(0));
			for (int i = 1; i < samples.size(); i++) {
				sb.append("\n" + samples.get(i));
			}
		}
		sb.append("\n]");
		return sb.toString();
	}


	/** 
	 * Resample to n points. Linear interpolation. 
	 * Returns a new object. Does not modify the original object.
	 * @param n the number of points in the result
	 * @return the resampled Template.
	 */
	public Template resample(int n) {
		
		Template t = new Template(gestureId);

		if (n < 0) n = 0;

		// return empty vector if no points in vector
		int m = samples.size();
		if (m <= 0) return t;

		// vector has only a single point
		if (m == 1) {
			Point3D p = new Point3D(samples.get(0));
			for (int i = 0; i < n; i++) {
				t.samples.add(p);
			}
			return t;
		}

		// at least 2 points in vector
		double I = pathLength() / (n-1);
		double D = 0;
		Point3D pp = samples.get(0);

		t.samples.add(new Point3D(samples.get(0))); // add first point of original vector

		for (int i = 1; i < m && t.samples.size() < n-1; i++) {
			Point3D p = samples.get(i);
			double d = distance(pp, p);
			if (d > 0 && D + d > I) {
				double delta = (I - D) / d;
				p = new Point3D(
						pp.x + delta * (p.x - pp.x), 
						pp.y + delta * (p.y - pp.y), 
						pp.z + delta * (p.z - pp.z));
				t.samples.add(p);
				D = 0;
				i--;
			} else {
				D += d;
			}
			pp = p;
		}

		t.samples.add(new Point3D(samples.get(m-1))); // add last point of original vector

		assert(t.samples.size() == n);

		return t;
	}

	
	/**
	 * Length of the trace.
	 * @param trace
	 * @return length of the trace
	 */
	public double pathLength() {
		int n = samples.size();
		double length = 0;
		for (int i = 1; i < n; i++) {
			length += distance(samples.get(i-1), samples.get(i));
		}
		return length;
	}


	/**
	 * Euclidean distance between points a and b.
	 * @param a
	 * @param b
	 * @return distance between points a and b
	 */
	public double distance(Point3D a, Point3D b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		double dz = a.z - b.z;
		return (double) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}


	/**
	 * The centroid is the point (average x-coordinate, average y-coordinate).
	 * @param points
	 * @return centroid of points
	 */
	public Point3D centroid() {
		int n = samples.size();
		if (n <= 0) return new Point3D(0,0,0);
		double cx = 0;
		double cy = 0;
		double cz = 0;
		for (Point3D p : samples) {
			cx += p.x;
			cy += p.y;
			cz += p.z;
		}
		double a = 1.0f / n;
		cx *= a;
		cy *= a;
		cz *= a;
		return new Point3D(cx, cy, cz);
	}


	/**
	 * Move every point by the same amount.
	 * @param points
	 * @param translationVector
	 */
	public void translate(Point3D translationVector) {
		for (Point3D p : samples) {
			p.x += translationVector.x;
			p.y += translationVector.y;
			p.z += translationVector.z;
		}
	}

	
	/**
	 * Consider the argument vector of n 2D-points as a float vector of 2n floats (R^(2n)).
	 * Normalize the argument vector of 2n floats to a unit vector (of dimension 2n).
	 * @param points
	 */
	public void normalize() {
		float mag = 0;
		for (Point3D p : samples) {
			mag += p.x * p.x + p.y * p.y + p.z * p.z;
		}
		mag = (float) Math.sqrt(mag);
		if (mag != 0.0) mag = 1.0f / mag;
		for (Point3D p : samples) {
			p.x *= mag;
			p.y *= mag;
			p.z *= mag;
		}
	}


	/**
	 * Compute the deltas between neighboring points. Replace samples array with the result.
	 */
	public void delta() {
		int n = samples.size();
		Vector<Point3D> d = new Vector<Point3D>();
		for (int i = 1; i < n; i++) {
			Point3D p = samples.get(i-1);
			Point3D q = samples.get(i);
			d.add(new Point3D(q.x-p.x, q.y-p.y, q.z-p.z));
		}
		samples = d;
	}

}
