package hm.orz.chaos114.android.tumekyouen.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 詰め共円のステージ情報モデル
 *
 * @author noboru
 */
@Data
public class TumeKyouenModel implements Serializable {

    public static final Integer CLEAR = 1;

    /** ステージ番号 */
    private int stageNo;

    /** ステージサイズ */
    private int size;

    /** ステージ上の石の配置（配置箇所には1、それ以外は0） */
    private String stage;

    /** 作者 */
    private String creator;

    /** クリアフラグ（クリア時に1、それ以外は0） */
    private int clearFlag;

    /** クリア日付 */
    private Date clearDate;
}
