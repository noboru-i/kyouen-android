package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.TypeAdapter

import java.util.Date

@AutoValue
abstract class AddAllResponse {
    abstract fun message(): String

    abstract fun data(): List<Stage>

    @AutoValue
    abstract class Stage {
        abstract fun stageNo(): Int

        abstract fun clearDate(): Date

        companion object {

            fun typeAdapter(gson: Gson): TypeAdapter<Stage> {
                return AutoValue_AddAllResponse_Stage.GsonTypeAdapter(gson)
            }
        }
    }

    companion object {

        fun typeAdapter(gson: Gson): TypeAdapter<AddAllResponse> {
            return AutoValue_AddAllResponse.GsonTypeAdapter(gson)
        }
    }
}
