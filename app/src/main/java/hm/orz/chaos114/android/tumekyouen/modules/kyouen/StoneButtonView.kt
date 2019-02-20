package hm.orz.chaos114.android.tumekyouen.modules.kyouen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import androidx.appcompat.widget.AppCompatImageButton
import hm.orz.chaos114.android.tumekyouen.R

class StoneButtonView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private val paint = Paint()

    enum class ButtonState {
        NONE, BLACK, WHITE
    }

    override fun onDraw(canvas: Canvas) {

        val bitmapSize = canvas.width

        paint.color = Color.rgb(128, 128, 128)
        paint.strokeWidth = 4f

        canvas.drawLine((bitmapSize / 2).toFloat(), 0f, (bitmapSize / 2).toFloat(), bitmapSize.toFloat(), paint)
        canvas.drawLine(0f, (bitmapSize / 2).toFloat(), bitmapSize.toFloat(), (bitmapSize / 2).toFloat(), paint)

        paint.color = Color.rgb(32, 32, 32)
        paint.strokeWidth = 2f

        canvas.drawLine((bitmapSize / 2).toFloat(), 0f, (bitmapSize / 2).toFloat(), bitmapSize.toFloat(), paint)
        canvas.drawLine(0f, (bitmapSize / 2).toFloat(), bitmapSize.toFloat(), (bitmapSize / 2).toFloat(), paint)

        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        val widthSize = View.MeasureSpec.getSize(widthSpec)

        setMeasuredDimension(widthSize, widthSize)

        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode)
        heightSpec = View.MeasureSpec.makeMeasureSpec(widthSize, heightMode)

        super.onMeasure(widthSpec, heightSpec)
    }

    fun setState(state: ButtonState) {
        when (state) {
            StoneButtonView.ButtonState.WHITE -> setImageResource(R.drawable.circle_white)
            StoneButtonView.ButtonState.BLACK -> setImageResource(R.drawable.circle_black)
            StoneButtonView.ButtonState.NONE -> setImageResource(0)
        }
    }
}
