package hm.orz.chaos114.android.tumekyouen.modules.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import hm.orz.chaos114.android.tumekyouen.model.KyouenData

class OverlayView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val maxScreenWidth: Int
    private var size: Int = 0
    private var data: KyouenData? = null

    private val paint: Paint = Paint().apply {
        color = Color.rgb(128, 128, 128)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    init {
        val display = (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val displaySize = Point()
        display.getSize(displaySize)
        maxScreenWidth = displaySize.x

        // stop propagation
        setOnClickListener { v ->
            // no-op
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        setMeasuredDimension(widthSize, widthSize)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode)
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(widthSize, heightMode)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data == null) {
            return
        }

        val offset = (maxScreenWidth / size).toDouble()
        if (data!!.lineKyouen) {
            // 直線の場合
            val line = data!!.line
            val startX: Float
            val startY: Float
            val stopX: Float
            val stopY: Float
            if (line!!.a == 0.0) {
                // x軸と平行な場合
                startX = 0f
                startY = (line.getY(0.0) * offset + offset / 2).toFloat()
                stopX = maxScreenWidth.toFloat()
                stopY = (line.getY(0.0) * offset + offset / 2).toFloat()
            } else if (line.b == 0.0) {
                // y軸と平行な場合
                startX = (line.getX(0.0) * offset + offset / 2).toFloat()
                startY = 0f
                stopX = (line.getX(0.0) * offset + offset / 2).toFloat()
                stopY = maxScreenWidth.toFloat()
            } else {
                // 上記以外の場合
                if (-1 * line.c / line.b > 0) {
                    startX = 0f
                    startY = (line.getY(-0.5) * offset + offset * 2 / 4).toFloat()
                    stopX = maxScreenWidth.toFloat()
                    stopY = (line.getY(size - 0.5) * offset + offset * 2 / 4).toFloat()
                } else {
                    startX = (line.getX(-0.5) * offset + offset * 2 / 4).toFloat()
                    startY = 0f
                    stopX = (line.getX(size - 0.5) * offset + offset * 2 / 4).toFloat()
                    stopY = maxScreenWidth.toFloat()
                }
            }
            canvas.drawLine(startX, startY, stopX, stopY, paint)
        } else {
            // 円の場合
            val cx = (data!!.center!!.x * offset + offset / 2).toFloat()
            val cy = (data!!.center!!.y * offset + offset / 2).toFloat()
            val radius = (data!!.radius * offset).toFloat()
            canvas.drawCircle(cx, cy, radius, paint)
        }
    }

    fun setData(aSize: Int, aData: KyouenData) {
        this.size = aSize
        this.data = aData
    }
}
