package hm.orz.chaos114.android.tumekyouen.modules.create

import android.content.Context
import android.util.AttributeSet

import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.KyouenData
import hm.orz.chaos114.android.tumekyouen.model.Point
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.modules.common.KyouenView
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.StoneButtonView
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import timber.log.Timber

class CreateKyouenView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : KyouenView(context, attrs) {

    private var soundManager: SoundManager? = null
    private var listener: CreateKyouenViewListener? = null

    interface CreateKyouenViewListener {
        fun onKyouen(kyouenData: KyouenData)
        fun onAddStone()
    }

    fun inject(soundManager: SoundManager, listener: CreateKyouenViewListener) {
        this.soundManager = soundManager
        this.listener = listener
    }

    override fun setData(stageModel: TumeKyouenModel) {
        super.setData(stageModel)
    }

    override fun onClickButton(b: StoneButtonView) {
        val index = buttons.indexOf(b)

        val col = index % gameModel.size
        val row = index / gameModel.size
        gameModel.putStone(col, row)
        soundManager!!.play(R.raw.se_maoudamashii_se_finger01)

        applyButtons()

        if (gameModel.blackStoneCount >= 4) {
            val kyouenData = gameModel.hasKyouen()
            Timber.d("kyouenData = %s", kyouenData)
            if (kyouenData != null) {
                applyWhiteStones(kyouenData)
                listener!!.onKyouen(kyouenData)
            }
        }
        listener!!.onAddStone()
    }

    fun popStone() {
        gameModel.popStone()
        applyBlackStones()
    }

    private fun applyWhiteStones(kyouenData: KyouenData) {
        for (point in kyouenData.points) {
            gameModel.switchColor(point.x.toInt(), point.y.toInt())
        }
        applyButtons()
    }

    private fun applyBlackStones() {
        gameModel.reset()
        applyButtons()
    }
}
