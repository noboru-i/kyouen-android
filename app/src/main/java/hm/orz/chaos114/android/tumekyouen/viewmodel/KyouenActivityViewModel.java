package hm.orz.chaos114.android.tumekyouen.viewmodel;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;

/**
 * KyouenActivity用のViewModel。
 */
public final class KyouenActivityViewModel {

    private TumeKyouenModel tumeKyouenModel;

    private Context context;

    public KyouenActivityViewModel(TumeKyouenModel tumeKyouenModel, Context context) {
        this.tumeKyouenModel = tumeKyouenModel;
        this.context = context;
    }

    public String getTitleStageNo() {
        return context.getString(R.string.stage_no, tumeKyouenModel.getStageNo());
    }

    public String getTitleCreator() {
        return context.getString(R.string.creator, tumeKyouenModel.getCreator());
    }

    @ColorRes
    public int getStageNoTextColor() {
        boolean isClear = tumeKyouenModel.getClearFlag() == TumeKyouenModel.CLEAR;

        return ContextCompat.getColor(context, isClear ? R.color.text_clear : R.color.text_not_clear);
    }

    public boolean hasPrev() {
        return tumeKyouenModel.getStageNo() > 1;
    }
}
