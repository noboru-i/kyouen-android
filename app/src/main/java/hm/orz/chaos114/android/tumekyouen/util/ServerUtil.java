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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import rx.Observable;
import timber.log.Timber;

/**
 * APサーバと通信するユーティリティクラス
 */
public final class ServerUtil {

    /** cookie情報を保持 */
    private static List<Cookie> cookies = new ArrayList<>();

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

    public static Observable<AddAllResponse> addAll(TumeKyouenService tumeKyouenService,
                                                    List<TumeKyouenModel> stages) {
        // ステージデータを送信
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
        return tumeKyouenService.addAll(sendData.toString());
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
