package hm.orz.chaos114.android.tumekyouen.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import io.reactivex.Single

/**
 * APサーバと通信するユーティリティクラス
 */
object ServerUtil {

    fun addAll(tumeKyouenService: TumeKyouenService,
               stages: List<TumeKyouenModel>): Single<AddAllResponse> {
        // ステージデータを送信
        val sendData = JSONArray()
        val simpleDateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        for (stageModel in stages) {
            val map = JSONObject()
            try {
                map.put("stageNo", Integer.toString(stageModel.stageNo()))
                map.put("clearDate",
                        simpleDateFormat.format(stageModel.clearDate()))
            } catch (e: JSONException) {
                continue
            }

            sendData.put(map)
        }
        return tumeKyouenService.addAll(sendData.toString())
    }
}
