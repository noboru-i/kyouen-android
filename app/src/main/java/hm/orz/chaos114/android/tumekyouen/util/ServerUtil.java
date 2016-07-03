package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;

/**
 * APサーバと通信するユーティリティクラス
 */
public final class ServerUtil {

    private static final String TAG = ServerUtil.class.getSimpleName();

    /** 最大試行回数 */
    private static final int MAX_ATTEMPTS = 5;
    /** 次の送信までの待ち時間初期値 */
    private static final int BACKOFF_MILLI_SECONDS = 2000;

    /** cookie情報を保持 */
    private static List<Cookie> cookies = new ArrayList<>();

    /**
     * ユーザ登録を行う。
     *
     * @param context     コンテキスト
     * @param token       twitterトークン
     * @param tokenSecret twitter秘密鍵
     * @throws IOException ネットワーク例外
     */
    public static void registUser(final Context context, final String token,
                                  final String tokenSecret) throws IOException {
        String url = context.getString(R.string.server_url) + "/page/api_login";
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("token_secret", tokenSecret);

        post(url, params);
    }

    /**
     * ステージクリア情報を送信する。
     *
     * @param context    コンテキスト
     * @param stageModel クリアステージ情報
     */
    public static void addStageUser(final Context context,
                                    final TumeKyouenModel stageModel) {
        String url = context.getString(R.string.server_url) + "/page/add";
        Map<String, String> params = new HashMap<>();
        params.put("stageNo", Integer.toString(stageModel.getStageNo()));

        try {
            post(url, params);
        } catch (Exception e) {
            Log.e(TAG, "クリア情報の送信に失敗", e);
        }
    }

    /**
     * すべてのデータを送信する。
     *
     * @param context コンテキスト
     * @param stages  クリアステージ情報リスト
     * @return 返却JSON
     */
    public static List<TumeKyouenModel> addAllStageUser(final Context context,
                                                        List<TumeKyouenModel> stages) {
        // 全ステージを送信する
        String url = context.getString(R.string.server_url) + "/page/add_all";
        JSONArray sendData = new JSONArray();
        DateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (TumeKyouenModel stageModel : stages) {
            JSONObject map = new JSONObject();
            try {
                map.put("stageNo", Integer.toString(stageModel.getStageNo()));
                map.put("clearDate",
                        simpleDateFormat.format(stageModel.getClearDate()));
            } catch (JSONException e) {
                continue;
            }
            sendData.put(map);
        }
        Map<String, String> params = new HashMap<>();
        params.put("data", sendData.toString());

        String responseString;
        try {
            responseString = post(url, params);
        } catch (Exception e) {
            Log.e(TAG, "クリア情報の送信に失敗", e);
            return null;
        }

        List<TumeKyouenModel> resultList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(responseString);
            JSONArray dataArray = obj.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject data = dataArray.getJSONObject(i);
                int stageNo = data.getInt("stageNo");
                Date clearDate = simpleDateFormat.parse(data
                        .getString("clearDate"));
                TumeKyouenModel model = new TumeKyouenModel();
                model.setStageNo(stageNo);
                model.setClearDate(clearDate);
                resultList.add(model);
            }
        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }

        return resultList;
    }

    private static void get(final String endpoint,
                            final Map<String, String> params) throws IOException {
        Log.i(TAG, "endpoint = " + endpoint);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(endpoint);
        BasicHttpParams getParams = new BasicHttpParams();
        for (String name : params.keySet()) {
            String value = params.get(name);
            getParams.setParameter(name, value);
        }
        httpGet.setParams(getParams);

        // Cookieの登録
        for (Cookie c : cookies) {
            httpClient.getCookieStore().addCookie(c);
        }

        HttpResponse response = httpClient.execute(httpGet);
        Log.i(TAG, "response = " + EntityUtils.toString(response.getEntity()));
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException("Post failed. statusCode=" + statusCode);
        }

        // Cookie取得
        cookies = httpClient.getCookieStore().getCookies();
        Log.d(TAG, "cookies = " + cookies);
    }

    /**
     * APサーバにPOSTリクエストを発行する。
     *
     * @param endpoint リクエストURL
     * @param params   リクエストパラメータ
     * @return レスポンス文字列
     * @throws IOException 通信例外
     */
    private static String post(final String endpoint,
                               final Map<String, String> params) throws IOException {
        Log.i(TAG, "endpoint = " + endpoint);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(endpoint);
        List<NameValuePair> post_params = new ArrayList<NameValuePair>();
        for (String name : params.keySet()) {
            String value = params.get(name);
            post_params.add(new BasicNameValuePair(name, value));
        }

        // Cookieの登録
        for (Cookie c : cookies) {
            httpClient.getCookieStore().addCookie(c);
        }

        try {
            // 送信パラメータのエンコードを指定
            httpPost.setEntity(new UrlEncodedFormEntity(post_params, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
        HttpResponse response = httpClient.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity());
        Log.i(TAG, "response = " + responseString);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException("Post failed. statusCode=" + statusCode);
        }

        // Cookie取得
        cookies = httpClient.getCookieStore().getCookies();
        Log.d(TAG, "cookies = " + cookies);
        return responseString;
    }
}
