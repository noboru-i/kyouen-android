package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue

import java.util.ArrayList

@AutoValue
abstract class GameModel {

    val blackStoneCount: Int
        get() = stonePoints().size

    val whiteStoneCount: Int
        get() = whiteStonePoints().size

    val stageStateForSend: String
        get() {
            val builder = StringBuilder()
            for (i in 0 until size() * size()) {
                val col = i % size()
                val row = i / size()
                if (hasStone(col, row)) {
                    builder.append("1")
                } else {
                    builder.append("0")
                }
            }
            return builder.toString()
        }

    val isKyouen: KyouenData?
        get() {
            if (whiteStonePoints().size < 4) {
                return null
            }
            val p1 = whiteStonePoints()[0]
            val p2 = whiteStonePoints()[1]
            val p3 = whiteStonePoints()[2]
            val p4 = whiteStonePoints()[3]

            return isKyouen(p1, p2, p3, p4)

        }

    abstract fun size(): Int

    abstract fun startState(): String

    abstract fun stonePoints(): MutableList<Point>

    abstract fun whiteStonePoints(): MutableList<Point>

    fun switchColor(x: Int, y: Int) {
        val p = Point.create(x.toDouble(), y.toDouble())
        if (isSelected(x, y)) {
            whiteStonePoints().remove(p)
        } else {
            whiteStonePoints().add(p)
        }
    }

    fun putStone(x: Int, y: Int) {
        val p = Point.create(x.toDouble(), y.toDouble())
        stonePoints().add(p)
    }

    fun popStone() {
        if (stonePoints().isEmpty()) {
            return
        }
        stonePoints().removeAt(stonePoints().size - 1)
    }

    fun isSelected(x: Int, y: Int): Boolean {
        val p = Point.create(x.toDouble(), y.toDouble())
        return whiteStonePoints().contains(p)
    }

    fun hasStone(x: Int, y: Int): Boolean {
        return stonePoints().contains(Point.create(x.toDouble(), y.toDouble()))
    }

    fun reset() {
        whiteStonePoints().clear()
    }

    fun hasKyouen(): KyouenData? {
        for (i in 0 until stonePoints().size - 3) {
            val p1 = stonePoints()[i]
            for (j in i + 1 until stonePoints().size - 2) {
                val p2 = stonePoints()[j]
                for (k in j + 1 until stonePoints().size - 1) {
                    val p3 = stonePoints()[k]
                    for (l in k + 1 until stonePoints().size) {
                        val p4 = stonePoints()[l]
                        val kyouen = isKyouen(p1, p2, p3, p4)
                        if (kyouen != null) {
                            return kyouen
                        }
                    }
                }
            }
        }
        return null
    }

    private fun isKyouen(p1: Point, p2: Point, p3: Point, p4: Point): KyouenData? {
        // p1,p2の垂直二等分線を求める
        val l12 = getMidperpendicular(p1, p2)
        // p2,p3の垂直二等分線を求める
        val l23 = getMidperpendicular(p2, p3)

        // 交点を求める
        val intersection123 = getIntersection(l12, l23)
        if (intersection123 == null) {
            // p1,p2,p3が直線上に存在する場合
            val l34 = getMidperpendicular(p3, p4)
            val intersection234 = getIntersection(l23, l34)
                    ?: // p2,p3,p4が直線状に存在する場合
                    return KyouenData.create(p1, p2, p3, p4, Line.create(p1, p2))
        } else {
            val dist1 = getDistance(p1, intersection123)
            val dist2 = getDistance(p4, intersection123)
            if (Math.abs(dist1 - dist2) < 0.0000001) {
                return KyouenData.create(p1, p2, p3, p4, intersection123, dist1)
            }
        }
        return null
    }

    /**
     * 2点間の距離を求める。
     *
     * @param p1 座標1
     * @param p2 座標2
     * @return 距離
     */
    private fun getDistance(p1: Point, p2: Point): Double {
        val dist = p1.difference(p2)

        return dist.abs
    }

    /**
     * 2直線の交点を求める。
     *
     * @param l1 直線1
     * @param l2 直線2
     * @return 交点座標（交点が存在しない場合、null）
     */
    private fun getIntersection(l1: Line, l2: Line): Point? {
        val f1 = l1.p2().x() - l1.p1().x()
        val g1 = l1.p2().y() - l1.p1().y()
        val f2 = l2.p2().x() - l2.p1().x()
        val g2 = l2.p2().y() - l2.p1().y()

        val det = f2 * g1 - f1 * g2
        if (det == 0.0) {
            return null
        }

        val dx = l2.p1().x() - l1.p1().x()
        val dy = l2.p1().y() - l1.p1().y()
        val t1 = (f2 * dy - g2 * dx) / det

        return Point.create(l1.p1().x() + f1 * t1, l1.p1().y() + g1 * t1)
    }

    /**
     * 2点の垂直二等分線を求める。
     *
     * @param p1 座標1
     * @param p2 座標2
     * @return 垂直二等分線
     */
    private fun getMidperpendicular(p1: Point, p2: Point): Line {
        val midpoint = getMidpoint(p1, p2)
        val dif = p1.difference(p2)
        val gradient = Point.create(dif.y(), -1 * dif.x())

        return Line.create(midpoint, midpoint.sum(gradient))
    }

    /**
     * 中点を求める。
     *
     * @param p1 座標1
     * @param p2 座標2
     * @return 中点座標
     */
    private fun getMidpoint(p1: Point, p2: Point): Point {

        return Point.create((p1.x() + p2.x()) / 2, (p1.y() + p2.y()) / 2)
    }

    companion object {

        fun create(aSize: Int, aStartState: String): GameModel {
            val stonePoints = ArrayList<Point>()
            val states = aStartState.toCharArray()
            for (i in states.indices) {
                if (states[i] == '0') {
                    continue
                }
                val col = i % aSize
                val row = i / aSize
                stonePoints.add(Point.create(col.toDouble(), row.toDouble()))
            }

            return AutoValue_GameModel(aSize, aStartState, stonePoints, ArrayList())
        }
    }
}
