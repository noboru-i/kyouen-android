package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue

@AutoValue
abstract class Point {

    internal val abs: Double
        get() = Math.sqrt(x() * x() + y() * y())

    abstract fun x(): Double

    abstract fun y(): Double

    internal fun difference(p2: Point): Point {
        return Point.create(x() - p2.x(), y() - p2.y())
    }

    internal fun sum(p2: Point): Point {
        return Point.create(x() + p2.x(), y() + p2.y())
    }

    companion object {

        fun create(x: Double, y: Double): Point {
            return AutoValue_Point(x, y)
        }
    }
}