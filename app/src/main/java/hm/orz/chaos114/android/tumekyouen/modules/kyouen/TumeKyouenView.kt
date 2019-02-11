package hm.orz.chaos114.android.tumekyouen.modules.kyouen

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet

import com.google.firebase.analytics.FirebaseAnalytics

import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.modules.common.KyouenView
import hm.orz.chaos114.android.tumekyouen.util.SoundManager

class TumeKyouenView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : KyouenView(context, attrs) {

    private var soundManager: SoundManager? = null
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun inject(soundManager: SoundManager, firebaseAnalytics: FirebaseAnalytics) {
        this.soundManager = soundManager
        this.firebaseAnalytics = firebaseAnalytics
    }

    override fun setData(stageModel: TumeKyouenModel) {
        super.setData(stageModel)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "stage")
        bundle.putString(FirebaseAnalytics.Param.VALUE, Integer.toString(stageModel.stageNo()))
        firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
    }

    override fun onClickButton(b: StoneButtonView) {
        val index = buttons.indexOf(b)

        val col = index % gameModel.size()
        val row = index / gameModel.size()
        if (!gameModel.hasStone(col, row)) {
            return
        }

        gameModel.switchColor(col, row)
        soundManager!!.play(R.raw.se_maoudamashii_se_finger01)

        applyButtons()
    }
}
