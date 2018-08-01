package hm.orz.chaos114.android.tumekyouen.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import io.reactivex.Single;

/**
 * APサーバと通信するユーティリティクラス
 */
public final class ServerUtil {

    public static Single<AddAllResponse> addAll(TumeKyouenService tumeKyouenService,
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
                map.put("stageNo", Integer.toString(stageModel.stageNo()));
                map.put("clearDate",
                        simpleDateFormat.format(stageModel.clearDate()));
            } catch (JSONException e) {
                continue;
            }
            sendData.put(map);
        }
        return tumeKyouenService.addAll(sendData.toString());
    }
}
