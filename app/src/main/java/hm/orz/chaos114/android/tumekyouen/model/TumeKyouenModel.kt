package hm.orz.chaos114.android.tumekyouen.model

import com.google.auto.value.AutoValue
import com.google.gson.Gson
import com.google.gson.TypeAdapter

import java.io.Serializable
import java.util.Date

/**
 * 詰め共円のステージ情報モデル
 */
@AutoValue
abstract class TumeKyouenModel : Serializable {

    /**
     * ステージ番号
     */
    abstract fun stageNo(): Int

    /**
     * ステージサイズ
     */
    abstract fun size(): Int

    /**
     * ステージ上の石の配置（配置箇所には1、それ以外は0）
     */
    abstract fun stage(): String

    /**
     * 作者
     */
    abstract fun creator(): String

    /**
     * クリアフラグ（クリア時に1、それ以外は0）
     */
    abstract fun clearFlag(): Int

    /**
     * クリア日付
     */
    abstract fun clearDate(): Date

    companion object {

        val CLEAR = 1

        fun create(stageNo: Int, size: Int, stage: String, creator: String, clearFlag: Int, clearDate: Date): TumeKyouenModel {
            return AutoValue_TumeKyouenModel(stageNo, size, stage, creator, clearFlag, clearDate)
        }

        fun typeAdapter(gson: Gson): TypeAdapter<TumeKyouenModel> {
            return AutoValue_TumeKyouenModel.GsonTypeAdapter(gson)
        }
    }
}
