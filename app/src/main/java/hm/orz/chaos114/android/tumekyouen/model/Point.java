package hm.orz.chaos114.android.tumekyouen.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Point {
    public abstract double x();

    public abstract double y();

    public static Point create(double x, double y) {
        return new AutoValue_Point(x, y);
    }

    double getAbs() {
        return Math.sqrt(x() * x() + y() * y());
    }

    @NonNull
    Point difference(Point p2) {
        return Point.create(x() - p2.x(), y() - p2.y());
    }

    @NonNull
    Point sum(Point p2) {
        return Point.create(x() + p2.x(), y() + p2.y());
    }
}