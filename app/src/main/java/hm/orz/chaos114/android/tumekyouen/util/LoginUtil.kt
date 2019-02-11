package hm.orz.chaos114.android.tumekyouen.util

import com.twitter.sdk.android.core.TwitterAuthToken

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUtil @Inject
constructor(private val preferenceUtil: PreferenceUtil, private val encryptionUtil: EncryptionUtil) {

    /**
     * ログイン情報を保存する。 nullが与えられた場合、クリアする
     *
     * @param authToken ログイン情報
     */
    fun saveLoginInfo(authToken: TwitterAuthToken?) {
        if (authToken == null) {
            // nullの場合、削除
            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN)
            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN_SECRET)
            return
        }

        // 値の保存
        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN,
                encryptionUtil.encrypt(authToken.token)!!)
        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN_SECRET,
                encryptionUtil.encrypt(authToken.secret)!!)
    }

    /**
     * ログイン情報返却する。 取得できなかった場合はnullを返却する。
     *
     * @return ログイン情報
     */
    fun loadLoginInfo(): TwitterAuthToken? {
        val token = encryptionUtil.decrypt(preferenceUtil
                .getString(PreferenceUtil.KEY_TOKEN))
        val tokenSecret = encryptionUtil.decrypt(preferenceUtil
                .getString(PreferenceUtil.KEY_TOKEN_SECRET))

        return if (token == null || tokenSecret == null) {
            // 取得できなかった場合はnullを返却
            null
        } else TwitterAuthToken(token, tokenSecret)
    }
}
