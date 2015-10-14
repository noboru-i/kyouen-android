package hm.orz.chaos114.android.tumekyouen.model;

import lombok.Getter;
import lombok.ToString;

/**
 * Ax+By+C=0を表現するクラス。
 */
@Getter
@ToString
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
        return -1 * (a * x + c) / b;
    }

    public double getX(double y) {
        return -1 * (b * y + c) / a;
    }
}