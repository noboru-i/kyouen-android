package hm.orz.chaos114.android.tumekyouen.modules.common

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.TableLayout
import android.widget.TableRow
import hm.orz.chaos114.android.tumekyouen.model.GameModel
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.StoneButtonView
import java.util.ArrayList

open class KyouenView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : TableLayout(context, attrs) {

    private var maxScreenWidth: Int = 0
    protected lateinit var buttons: MutableList<StoneButtonView>
    lateinit var gameModel: GameModel
        protected set

    init {
        setWindowSize()
        initViews()
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

    private fun setWindowSize() {
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val displaySize = Point()
        display.getSize(displaySize)
        maxScreenWidth = displaySize.x
    }

    open fun setData(stageModel: TumeKyouenModel) {
        gameModel = GameModel.create(stageModel.size, stageModel.stage)
        initButtons()
        applyButtons()
    }

    protected open fun onClickButton(b: StoneButtonView) {
        // no-op
    }

    private fun initViews() {
        buttons = ArrayList()
    }

    private fun initButtons() {
        buttons.clear()
        removeAllViews()
        for (i in 0 until gameModel.size) {
            val tableRow = TableRow(context)
            addView(tableRow)
            for (j in 0 until gameModel.size) {
                val button = StoneButtonView(context)
                val stoneSize = maxScreenWidth / gameModel.size
                buttons.add(button)
                tableRow.addView(button, stoneSize, stoneSize)

                button.setOnClickListener { v -> onClickButton(v as StoneButtonView) }
            }
        }
    }

    protected fun applyButtons() {
        val size = gameModel.size
        for (row in 0 until size) {
            for (col in 0 until size) {
                val button = buttons[row * size + col]
                if (gameModel.hasStone(col, row)) {
                    if (gameModel.isSelected(col, row)) {
                        button.setState(StoneButtonView.ButtonState.WHITE)
                    } else {
                        button.setState(StoneButtonView.ButtonState.BLACK)
                    }
                } else {
                    button.setState(StoneButtonView.ButtonState.NONE)
                }
            }
        }
    }

    fun reset() {
        gameModel.reset()
        applyButtons()
    }

    override fun setClickable(clickable: Boolean) {
        buttons.forEach { button ->
            button.isClickable = clickable
        }
    }
}
