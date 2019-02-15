package hm.orz.chaos114.android.tumekyouen.modules.title

import android.content.Context
import android.graphics.drawable.Drawable

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.util.SoundManager

class TitleActivityViewModel internal constructor(
        private val context: Context,
        private val stageCountModel: StageCountModel,
        private val soundManager: SoundManager
) {

    val displayStageCount: String
        get() = context.getString(R.string.stage_count,
                stageCountModel.clearStageCount,
                stageCountModel.stageCount)

    val soundResource: Drawable?
        get() {
            val playable = soundManager.isPlayable
            @DrawableRes val imageRes = if (playable) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            return ContextCompat.getDrawable(context, imageRes)
        }
}
