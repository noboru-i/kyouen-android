package hm.orz.chaos114.android.tumekyouen.model

import java.io.Serializable
import java.util.Date

data class TumeKyouenModel(
        val stageNo: Int,
        val size: Int,
        val stage: String,
        val creator: String,
        val clearFlag: Int,
        val clearDate: Date
) : Serializable {

    companion object {
        const val CLEAR = 1
    }
}
