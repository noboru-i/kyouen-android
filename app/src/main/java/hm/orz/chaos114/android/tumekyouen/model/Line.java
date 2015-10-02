package hm.orz.chaos114.android.tumekyouen.model;

/**
 * Ax+By+C=0を表現するクラス。
 */
public class Line {
	Point p1;
	Point p2;
	double a;
	double b;
	double c;

	public Line(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;

		a = p1.y - p2.y;
		b = p2.x - p1.x;
		c = p1.x * p2.y - p2.x * p1.y;
	}

	public double getY(double x) {
		double y = -1 * (a * x + c) / b;

		return y;
	}

	public double getX(double y) {
		double x = -1 * (b * y + c) / a;

		return x;
	}

	public Point getP1() {
		return p1;
	}

	public Point getP2() {
		return p2;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public double getC() {
		return c;
	}

	@Override
	public String toString() {
		return "Line [p1=" + p1 + ", p2=" + p2 + ", a=" + a + ", b=" + b
				+ ", c=" + c + "]";
	}
}