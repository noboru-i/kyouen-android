package hm.orz.chaos114.android.tumekyouen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 共円情報を表現するクラス。
 *
 * @author noboru
 */
public class KyouenData {
    List<Point> points;
    boolean isLine = false;
    Point center;
    double radius;
    Line line;

    public KyouenData(Point p1, Point p2, Point p3, Point p4, Line aLine) {
        this(p1, p2, p3, p4, true, null, 0, aLine);
    }

    public KyouenData(Point p1, Point p2, Point p3, Point p4, Point aCenter,
                      double aRadius) {
        this(p1, p2, p3, p4, false, aCenter, aRadius, null);
    }

    public KyouenData(Point p1, Point p2, Point p3, Point p4,
                      boolean aIsLine, Point aCenter, double aRadius, Line aLine) {
        this.points = new ArrayList<Point>();
        this.points.add(p1);
        this.points.add(p2);
        this.points.add(p3);
        this.points.add(p4);
        this.isLine = aIsLine;
        this.center = aCenter;
        this.radius = aRadius;
        this.line = aLine;
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean isLine() {
        return isLine;
    }

    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Line getLine() {
        return line;
    }

}