package hm.orz.chaos114.android.tumekyouen.model;

import android.support.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Point {
    double x;
    double y;

    double getAbs() {
        return Math.sqrt(x * x + y * y);
    }

    @NonNull
    Point difference(Point p2) {
        return new Point(x - p2.x, y - p2.y);
    }

    @NonNull
    Point sum(Point p2) {
        return new Point(x + p2.x, y + p2.y);
    }
}