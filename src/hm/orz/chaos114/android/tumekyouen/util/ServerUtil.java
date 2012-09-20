package hm.orz.chaos114.android.tumekyouen.util;

import hm.orz.chaos114.android.tumekyouen.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * APサーバと通信するユーティリティクラス
 */
public final class ServerUtil {

	private static final String TAG = ServerUtil.class.getSimpleName();

	/** 最大試行回数 */
	private static final int MAX_ATTEMPTS = 5;
	/** 次の送信までの待ち時間初期値 */
	private static final int BACKOFF_MILLI_SECONDS = 2000;

	/**
	 * GCM用の登録IDをAPサーバに登録する。
	 * 
	 * @param context コンテキスト
	 * @param regId 登録ID
	 * @return 登録に成功した場合true
	 */
	public static boolean regist(final Context context, final String regId) {
		Log.i(TAG, "#regist regId = " + regId);
		String url = context.getString(R.string.server_url) + "/gcm/regist";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		long backoff = BACKOFF_MILLI_SECONDS;
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			try {
				post(url, params);
				GCMRegistrar.setRegisteredOnServer(context, true);
				Log.i(TAG, "regist success");
				return true;
			} catch (IOException e) {
				Log.e(TAG, "regist not success", e);
				// サーバエラー時
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// abort
					Thread.currentThread().interrupt();
					return false;
				}
				// バックオフ値を倍加
				backoff *= 2;
			}
		}
		return false;
	}

	/**
	 * GCM用の登録IDをAPサーバより解除する。
	 * 
	 * @param context コンテキスト
	 * @param regId 登録ID
	 */
	public static void unregist(final Context context, final String regId) {
		String url = context.getString(R.string.server_url) + "/gcm/unregist";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		try {
			post(url, params);
			GCMRegistrar.setRegisteredOnServer(context, false);
		} catch (IOException e) {
			// GCMからは解除されたが、APサーバには登録されている状態
			// APサーバ側で送信時に"NotRegistered"エラーを検出可能なため、処理なし
		}
	}

	/**
	 * APサーバにPOSTリクエストを発行する。
	 * 
	 * @param endpoint リクエストURL
	 * @param params リクエストパラメータ
	 * @throws IOException 通信例外
	 */
	private static void post(final String endpoint, final Map<String, String> params)
			throws IOException {
		Log.i(TAG, "endpoint = " + endpoint);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(endpoint);
		List<NameValuePair> post_params = new ArrayList<NameValuePair>();
		for (String name : params.keySet()) {
			String value = params.get(name);
			post_params.add(new BasicNameValuePair(name, value));
		}
		try {
			// 送信パラメータのエンコードを指定
			httpPost.setEntity(new UrlEncodedFormEntity(post_params, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		HttpResponse response = httpClient.execute(httpPost);
		Log.i(TAG, "response = " + EntityUtils.toString(response.getEntity()));
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			throw new IOException("Post failed. statusCode=" + statusCode);
		}
	}
}
