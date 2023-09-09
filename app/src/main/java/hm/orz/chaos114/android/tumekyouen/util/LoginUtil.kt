package hm.orz.chaos114.android.tumekyouen.util

//import com.twitter.sdk.android.core.TwitterAuthToken

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUtil @Inject constructor(
    private val preferenceUtil: PreferenceUtil,
    private val encryptionUtil: EncryptionUtil
) {

//    fun saveLoginInfo(authToken: TwitterAuthToken?) {
//        if (authToken == null) {
//            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN)
//            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN_SECRET)
//            return
//        }
//
//        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN,
//            encryptionUtil.encrypt(authToken.token)!!)
//        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN_SECRET,
//            encryptionUtil.encrypt(authToken.secret)!!)
//    }
//
//    fun loadLoginInfo(): TwitterAuthToken? {
//        val token = encryptionUtil.decrypt(preferenceUtil.getString(PreferenceUtil.KEY_TOKEN))
//        val tokenSecret = encryptionUtil.decrypt(preferenceUtil.getString(PreferenceUtil.KEY_TOKEN_SECRET))
//
//        return if (token == null || tokenSecret == null) {
//            null
//        } else TwitterAuthToken(token, tokenSecret)
//    }
}
