package hm.orz.chaos114.android.tumekyouen.model

data class Point(
        val x: Double,
        val y: Double
) {

    internal val abs: Double
        get() = Math.sqrt(x * x + y * y)

    internal fun difference(p2: Point): Point {
        return Point.create(x - p2.x, y - p2.y)
    }

    internal fun sum(p2: Point): Point {
        return Point.create(x + p2.x, y + p2.y)
    }

    companion object {

        fun create(x: Double, y: Double): Point {
            return Point(x, y)
        }
    }
}
