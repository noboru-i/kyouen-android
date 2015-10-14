package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {
    /** 鍵のbit数 */
    private static final int ENCRYPT_KEY_LENGTH = 128;

    /** 暗号化キー */
    private final Key key;

    /**
     * コンストラクタ。
     * 暗号化キーを生成、もしくは復元します。
     *
     * @param context コンテキスト
     */
    public EncryptionUtil(Context context) {
        context = context.getApplicationContext();

        // Preferenceから暗号化キーを取得
        PreferenceUtil preferenceUtil = new PreferenceUtil(context);
        String keyStr = preferenceUtil.getString(PreferenceUtil.KEY_SECRET_KEY);

        if (keyStr == null) {
            // Preferenceから取得できなかった場合

            // 暗号化キーを生成
            key = generateKey();
            // 生成したキーを保存
            String base64Key = Base64.encodeToString(key.getEncoded(),
                    Base64.URL_SAFE | Base64.NO_WRAP);
            preferenceUtil.putString(PreferenceUtil.KEY_SECRET_KEY, base64Key);
        } else {
            // Preferenceから取得できた場合

            // キーを復元
            byte[] keyBytes = Base64.decode(keyStr, Base64.URL_SAFE
                    | Base64.NO_WRAP);
            key = new SecretKeySpec(keyBytes, "AES");
        }
    }

    /**
     * 暗号化キーを生成する。
     *
     * @return 暗号化キー
     */
    private static Key generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            generator.init(ENCRYPT_KEY_LENGTH, random);
            return generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 暗号化した文字列を返却する。
     *
     * @param input 入力文字列
     * @return 暗号化した文字列
     */
    public String encrypt(String input) {
        if (input == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(input.getBytes());
            return Base64.encodeToString(result, Base64.URL_SAFE
                    | Base64.NO_WRAP);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 復号化した文字列を返却する。
     *
     * @param input 入力文字列
     * @return 復号化した文字列
     */
    public String decrypt(String input) {
        if (input == null) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Base64.decode(input, Base64.URL_SAFE
                    | Base64.NO_WRAP));
            return new String(result);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
