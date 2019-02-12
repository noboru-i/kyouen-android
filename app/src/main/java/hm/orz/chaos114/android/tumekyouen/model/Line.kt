package hm.orz.chaos114.android.tumekyouen.model

/**
 * a*x + b*y + c = 0
 */
data class Line(
        val p1: Point,
        val p2: Point,
        val a: Double,
        val b: Double,
        val c: Double
) {
    fun getY(x: Double): Double {
        return -1 * (a * x + c) / b
    }

    fun getX(y: Double): Double {
        return -1 * (b * y + c) / a
    }

    companion object {

        fun create(p1: Point, p2: Point): Line {
            val a = p1.y - p2.y
            val b = p2.x - p1.x
            val c = p1.x * p2.y - p2.x * p1.y

            return Line(p1, p2, a, b, c)
        }
    }
}
