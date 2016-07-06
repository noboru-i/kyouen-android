package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import timber.log.Timber;

/**
 * APサーバと通信するユーティリティクラス
 */
public final class ServerUtil {

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
            Timber.e(e, "クリア情報の送信に失敗");
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
            Timber.e(e, "クリア情報の送信に失敗");
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
        Timber.i("endpoint = %s", endpoint);
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
        Timber.i("response = %s", responseString);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new IOException("Post failed. statusCode=" + statusCode);
        }

        // Cookie取得
        cookies = httpClient.getCookieStore().getCookies();
        Timber.d("cookies = %s", cookies);
        return responseString;
    }
}
