package hm.orz.chaos114.android.tumekyouen.model;

import com.google.auto.value.AutoValue;

/**
 * Ax+By+C=0を表現するクラス。
 */
@AutoValue
public abstract class Line {
    public abstract Point p1();

    public abstract Point p2();

    public abstract double a();

    public abstract double b();

    public abstract double c();

    public static Line create(Point p1, Point p2) {
        double a = p1.y() - p2.y();
        double b = p2.x() - p1.x();
        double c = p1.x() * p2.y() - p2.x() * p1.y();

        return new AutoValue_Line(p1, p2, a, b, c);
    }

    public double getY(double x) {
        return -1 * (a() * x + c()) / b();
    }

    public double getX(double y) {
        return -1 * (b() * y + c()) / a();
    }
}
