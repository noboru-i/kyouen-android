package hm.orz.chaos114.android.tumekyouen.network

import com.google.gson.TypeAdapterFactory
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory

@GsonTypeAdapterFactory
abstract class GsonAutoValueAdapterFactory : TypeAdapterFactory {
    companion object {
        fun create(): TypeAdapterFactory {
            return AutoValueGson_GsonAutoValueAdapterFactory()
        }
    }
}
