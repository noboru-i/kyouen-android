package hm.orz.chaos114.android.tumekyouen.modules.kyouen

import android.content.Context

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel

class KyouenActivityViewModel internal constructor(
        private val tumeKyouenModel: TumeKyouenModel,
        private val context: Context
) {

    val titleStageNo: String
        get() = context.getString(R.string.stage_no, tumeKyouenModel.stageNo)

    val titleCreator: String
        get() = context.getString(R.string.creator, tumeKyouenModel.creator)

    val stageNoTextColor: Int
        @ColorInt
        get() {
            val isClear = tumeKyouenModel.clearFlag == TumeKyouenModel.CLEAR

            return ContextCompat.getColor(context, if (isClear) R.color.text_clear else R.color.text_not_clear)
        }

    fun hasPrev(): Boolean {
        return tumeKyouenModel.stageNo > 1
    }
}
