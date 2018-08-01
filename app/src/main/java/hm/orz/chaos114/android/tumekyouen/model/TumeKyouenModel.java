package hm.orz.chaos114.android.tumekyouen.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.Serializable;
import java.util.Date;

/**
 * 詰め共円のステージ情報モデル
 */
@AutoValue
public abstract class TumeKyouenModel implements Serializable {

    public static final Integer CLEAR = 1;

    /**
     * ステージ番号
     */
    public abstract int stageNo();

    /**
     * ステージサイズ
     */
    public abstract int size();

    /**
     * ステージ上の石の配置（配置箇所には1、それ以外は0）
     */
    public abstract String stage();

    /**
     * 作者
     */
    public abstract String creator();

    /**
     * クリアフラグ（クリア時に1、それ以外は0）
     */
    public abstract int clearFlag();

    /**
     * クリア日付
     */
    public abstract Date clearDate();

    public static TumeKyouenModel create(int stageNo, int size, String stage, String creator, int clearFlag, Date clearDate) {
        return new AutoValue_TumeKyouenModel(stageNo, size, stage, creator, clearFlag, clearDate);
    }

    public static TypeAdapter<TumeKyouenModel> typeAdapter(Gson gson) {
        return new AutoValue_TumeKyouenModel.GsonTypeAdapter(gson);
    }
}
