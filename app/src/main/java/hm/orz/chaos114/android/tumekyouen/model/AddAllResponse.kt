package hm.orz.chaos114.android.tumekyouen.model

import java.util.Date

data class AddAllResponse(
    val message: String,
    val data: List<Stage>
) {

    data class Stage(
        val stageNo: Int,
        val clearDate: Date
    )
}
