package hm.orz.chaos114.android.tumekyouen.util

import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceUtil @Inject constructor(
        private val sp: SharedPreferences
) {

    fun putString(key: String, value: String) {
        val editor = sp.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun putInt(key: String, value: Int) {
        val editor = sp.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor = sp.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getString(key: String): String? {
        return sp.getString(key, null)
    }

    fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    fun remove(key: String) {
        val editor = sp.edit()
        editor.remove(key)
        editor.commit()
    }

    companion object {

        // encryption key : String
        internal const val KEY_SECRET_KEY = "secret_key"

        // twitter token : String
        internal const val KEY_TOKEN = "token"

        // twitter token secret : String
        internal const val KEY_TOKEN_SECRET = "token_secret"

        // output sound ：boolean
        internal const val KEY_SOUND = "sound"

        // last shown stage number : int
        const val KEY_LAST_STAGE_NO = "last_stage_no"

        // stage creator name for send : String
        const val KEY_CREATOR_NAME = "creator_name"
    }
}
