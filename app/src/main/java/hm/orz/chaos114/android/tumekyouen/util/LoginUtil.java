package hm.orz.chaos114.android.tumekyouen.util;

import com.twitter.sdk.android.core.TwitterAuthToken;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.Nullable;

@Singleton
public class LoginUtil {
    private final PreferenceUtil preferenceUtil;

    private final EncryptionUtil encryptionUtil;

    @Inject
    public LoginUtil(PreferenceUtil preferenceUtil, EncryptionUtil encryptionUtil) {
        this.preferenceUtil = preferenceUtil;
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * ログイン情報を保存する。 nullが与えられた場合、クリアする
     *
     * @param authToken ログイン情報
     */
    public void saveLoginInfo(@Nullable TwitterAuthToken authToken) {
        if (authToken == null) {
            // nullの場合、削除
            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN);
            preferenceUtil.remove(PreferenceUtil.KEY_TOKEN_SECRET);
            return;
        }

        // 値の保存
        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN,
                encryptionUtil.encrypt(authToken.token));
        preferenceUtil.putString(PreferenceUtil.KEY_TOKEN_SECRET,
                encryptionUtil.encrypt(authToken.secret));
    }

    /**
     * ログイン情報返却する。 取得できなかった場合はnullを返却する。
     *
     * @return ログイン情報
     */
    @Nullable
    public TwitterAuthToken loadLoginInfo() {
        String token = encryptionUtil.decrypt(preferenceUtil
                .getString(PreferenceUtil.KEY_TOKEN));
        String tokenSecret = encryptionUtil.decrypt(preferenceUtil
                .getString(PreferenceUtil.KEY_TOKEN_SECRET));

        if (token == null || tokenSecret == null) {
            // 取得できなかった場合はnullを返却
            return null;
        }
        return new TwitterAuthToken(token, tokenSecret);
    }
}
