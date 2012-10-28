package hm.orz.chaos114.android.tumekyouen.util;

import twitter4j.auth.AccessToken;
import android.content.Context;

public class LoginUtil {
	/** Preferenceユーティリティ */
	private final PreferenceUtil preferenceUtil;

	/** 暗号化ユーティリティ */
	private final EncryptionUtil encryptionUtil;

	public LoginUtil(Context context) {
		preferenceUtil = new PreferenceUtil(context.getApplicationContext());
		encryptionUtil = new EncryptionUtil(context);
	}

	/**
	 * ログイン情報を保存する。 nullが与えられた場合、クリアする
	 * 
	 * @param loginInfo
	 */
	public void saveLoginInfo(AccessToken loginInfo) {
		if (loginInfo == null) {
			// nullの場合、削除
			preferenceUtil.remove(PreferenceUtil.KEY_TOKEN);
			preferenceUtil.remove(PreferenceUtil.KEY_TOKEN_SECRET);
			return;
		}

		// 値の保存
		preferenceUtil.putString(PreferenceUtil.KEY_TOKEN,
				encryptionUtil.encrypt(loginInfo.getToken()));
		preferenceUtil.putString(PreferenceUtil.KEY_TOKEN_SECRET,
				encryptionUtil.encrypt(loginInfo.getTokenSecret()));
	}

	/**
	 * ログイン情報返却する。 取得できなかった場合はnullを返却する。
	 * 
	 * @return ログイン情報
	 */
	public AccessToken loadLoginInfo() {
		String token = encryptionUtil.decrypt(preferenceUtil
				.getString(PreferenceUtil.KEY_TOKEN));
		String tokenSecret = encryptionUtil.decrypt(preferenceUtil
				.getString(PreferenceUtil.KEY_TOKEN_SECRET));

		if (token == null || tokenSecret == null) {
			// 取得できなかった場合はnullを返却
			return null;
		}
		return new AccessToken(token, tokenSecret);
	}
}
