package hm.orz.chaos114.android.tumekyouen.model;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 共円情報を表現するクラス。
 *
 * @author noboru
 */
@AutoValue
public abstract class KyouenData {
    public abstract List<Point> points();

    public abstract boolean lineKyouen();

    @Nullable
    public abstract Point center();

    public abstract double radius();

    @Nullable
    public abstract Line line();

    public static KyouenData create(Point p1, Point p2, Point p3, Point p4, Line aLine) {
        return create(p1, p2, p3, p4, true, null, 0, aLine);
    }

    public static KyouenData create(Point p1, Point p2, Point p3, Point p4, Point aCenter,
                                    double aRadius) {
        return create(p1, p2, p3, p4, false, aCenter, aRadius, null);
    }

    public static KyouenData create(Point p1, Point p2, Point p3, Point p4,
                                    boolean aIsLine, Point aCenter, double aRadius, Line aLine) {
        List<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        return new AutoValue_KyouenData(points, aIsLine, aCenter, aRadius, aLine);
    }
}