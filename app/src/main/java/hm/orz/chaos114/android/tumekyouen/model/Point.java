package hm.orz.chaos114.android.tumekyouen.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor(suppressConstructorProperties = true)
public class Point {
    double x;
    double y;

    public double getAbs() {
        return Math.sqrt(x * x + y * y);
    }

    public Point difference(Point p2) {
        return new Point(x - p2.x, y - p2.y);
    }

    public Point sum(Point p2) {
        return new Point(x + p2.x, y + p2.y);
    }
}