package hm.orz.chaos114.android.tumekyouen.model

import java.util.ArrayList

/**
 * 共円情報を表現するクラス。
 *
 * @author noboru
 */
data class KyouenData(
        val points: List<Point>,
        val lineKyouen: Boolean,
        val center: Point?,
        val radius: Double,
        val line: Line?
) {
    companion object {

        fun create(p1: Point, p2: Point, p3: Point, p4: Point, aLine: Line): KyouenData {
            return create(p1, p2, p3, p4, true, null, 0.0, aLine)
        }

        fun create(p1: Point, p2: Point, p3: Point, p4: Point, aCenter: Point,
                   aRadius: Double): KyouenData {
            return create(p1, p2, p3, p4, false, aCenter, aRadius, null)
        }

        fun create(p1: Point, p2: Point, p3: Point, p4: Point,
                   aIsLine: Boolean, aCenter: Point?, aRadius: Double, aLine: Line?): KyouenData {
            val points = ArrayList<Point>()
            points.add(p1)
            points.add(p2)
            points.add(p3)
            points.add(p4)
            return KyouenData(points, aIsLine, aCenter, aRadius, aLine)
        }
    }
}