package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue

/**
 * Ax+By+C=0を表現するクラス。
 */
@AutoValue
abstract class Line {
    abstract fun p1(): Point

    abstract fun p2(): Point

    abstract fun a(): Double

    abstract fun b(): Double

    abstract fun c(): Double

    fun getY(x: Double): Double {
        return -1 * (a() * x + c()) / b()
    }

    fun getX(y: Double): Double {
        return -1 * (b() * y + c()) / a()
    }

    companion object {

        fun create(p1: Point, p2: Point): Line {
            val a = p1.y() - p2.y()
            val b = p2.x() - p1.x()
            val c = p1.x() * p2.y() - p2.x() * p1.y()

            return AutoValue_Line(p1, p2, a, b, c)
        }
    }
}
