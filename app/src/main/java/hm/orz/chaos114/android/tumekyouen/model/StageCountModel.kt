package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue

@AutoValue
abstract class StageCountModel {

    abstract fun stageCount(): Int

    abstract fun clearStageCount(): Int

    companion object {

        fun create(stageCount: Int, clearStageCount: Int): StageCountModel {
            return AutoValue_StageCountModel(stageCount, clearStageCount)
        }
    }
}
