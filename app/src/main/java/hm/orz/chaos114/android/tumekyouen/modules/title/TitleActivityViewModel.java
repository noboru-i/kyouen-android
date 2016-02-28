package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

/**
 * TitleActivity用のViewModel。
 */
public final class TitleActivityViewModel {

    private StageCountModel stageCountModel;

    private Context context;

    public TitleActivityViewModel(StageCountModel stageCountModel, Context context) {
        this.stageCountModel = stageCountModel;
        this.context = context;
    }

    public String getDisplayStageCount() {
        return context.getString(R.string.stage_count,
                stageCountModel.getClearStageCount(),
                stageCountModel.getStageCount());
    }

    public Drawable getSoundResource() {
        boolean playable = SoundManager.getInstance(context).isPlayable();
        @DrawableRes int imageRes = playable ? R.drawable.sound_on : R.drawable.sound_off;
        return ContextCompat.getDrawable(context, imageRes);
    }
}
