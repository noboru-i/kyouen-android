package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.TypeAdapter

@AutoValue
abstract class LoginResult {
    abstract fun message(): String

    companion object {

        fun create(message: String): LoginResult {
            return AutoValue_LoginResult(message)
        }

        fun typeAdapter(gson: Gson): TypeAdapter<LoginResult> {
            return AutoValue_LoginResult.GsonTypeAdapter(gson)
        }
    }
}
