package hm.orz.chaos114.android.tumekyouen.util

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

import javax.inject.Inject

/**
 * Preferenceのユーティリティクラス
 *
 * @author ishikuranoboru
 */
class PreferenceUtil @Inject
constructor(private val sp: SharedPreferences) {

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

        // 暗号化キー：String
        internal val KEY_SECRET_KEY = "secret_key"

        // twitterトークン：String
        internal val KEY_TOKEN = "token"

        // twitterトークンシークレット：String
        internal val KEY_TOKEN_SECRET = "token_secret"

        // 音の出力要否：boolean
        internal val KEY_SOUND = "sound"

        // 最後に表示していたステージ番号：int
        val KEY_LAST_STAGE_NO = "last_stage_no"

        // ステージ作成者 : String
        val KEY_CREATOR_NAME = "creator_name"
    }
}
