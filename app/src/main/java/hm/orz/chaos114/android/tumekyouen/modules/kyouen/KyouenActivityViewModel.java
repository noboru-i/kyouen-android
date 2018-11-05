package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;

/**
 * KyouenActivity用のViewModel。
 */
public final class KyouenActivityViewModel {

    private TumeKyouenModel tumeKyouenModel;

    private Context context;

    KyouenActivityViewModel(TumeKyouenModel tumeKyouenModel, Context context) {
        this.tumeKyouenModel = tumeKyouenModel;
        this.context = context;
    }

    public String getTitleStageNo() {
        return context.getString(R.string.stage_no, tumeKyouenModel.stageNo());
    }

    public String getTitleCreator() {
        return context.getString(R.string.creator, tumeKyouenModel.creator());
    }

    @ColorInt
    public int getStageNoTextColor() {
        boolean isClear = tumeKyouenModel.clearFlag() == TumeKyouenModel.CLEAR;

        return ContextCompat.getColor(context, isClear ? R.color.text_clear : R.color.text_not_clear);
    }

    public boolean hasPrev() {
        return tumeKyouenModel.stageNo() > 1;
    }
}
