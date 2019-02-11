package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue

import java.util.ArrayList

/**
 * 共円情報を表現するクラス。
 *
 * @author noboru
 */
@AutoValue
abstract class KyouenData {
    abstract fun points(): List<Point>

    abstract fun lineKyouen(): Boolean

    abstract fun center(): Point?

    abstract fun radius(): Double

    abstract fun line(): Line?

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
            return AutoValue_KyouenData(points, aIsLine, aCenter, aRadius, aLine)
        }
    }
}