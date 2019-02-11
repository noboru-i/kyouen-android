package hm.orz.chaos114.android.tumekyouen.util

import android.util.Base64

import java.security.GeneralSecurityException
import java.security.Key
import java.security.SecureRandom

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionUtil
/**
 * コンストラクタ。
 * 暗号化キーを生成、もしくは復元します。
 */
@Inject
constructor(preferenceUtil: PreferenceUtil) {

    // 暗号化キー
    private val key: Key

    internal var preferenceUtil: PreferenceUtil? = null

    init {
        // Preferenceから暗号化キーを取得
        val keyStr = preferenceUtil.getString(PreferenceUtil.KEY_SECRET_KEY)

        if (keyStr == null) {
            // Preferenceから取得できなかった場合

            // 暗号化キーを生成
            key = generateKey()
            // 生成したキーを保存
            val base64Key = Base64.encodeToString(key.encoded,
                    Base64.URL_SAFE or Base64.NO_WRAP)
            preferenceUtil.putString(PreferenceUtil.KEY_SECRET_KEY, base64Key)
        } else {
            // Preferenceから取得できた場合

            // キーを復元
            val keyBytes = Base64.decode(keyStr, Base64.URL_SAFE or Base64.NO_WRAP)
            key = SecretKeySpec(keyBytes, "AES")
        }
    }

    /**
     * 暗号化した文字列を返却する。
     *
     * @param input 入力文字列
     * @return 暗号化した文字列
     */
    fun encrypt(input: String?): String? {
        if (input == null) {
            return null
        }

        try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val result = cipher.doFinal(input.toByteArray())
            return Base64.encodeToString(result, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }

    }

    /**
     * 復号化した文字列を返却する。
     *
     * @param input 入力文字列
     * @return 復号化した文字列
     */
    fun decrypt(input: String?): String? {
        if (input == null) {
            return null
        }

        try {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val result = cipher.doFinal(Base64.decode(input, Base64.URL_SAFE or Base64.NO_WRAP))
            return String(result)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }

    }

    companion object {
        // 鍵のbit数
        private val ENCRYPT_KEY_LENGTH = 128

        /**
         * 暗号化キーを生成する。
         *
         * @return 暗号化キー
         */
        private fun generateKey(): Key {
            try {
                val generator = KeyGenerator.getInstance("AES")
                val random = SecureRandom.getInstance("SHA1PRNG")
                generator.init(ENCRYPT_KEY_LENGTH, random)
                return generator.generateKey()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }
}
