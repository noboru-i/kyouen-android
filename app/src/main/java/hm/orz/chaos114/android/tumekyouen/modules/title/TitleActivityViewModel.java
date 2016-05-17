package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

/**
 * TitleActivity用のViewModel。
 */
public final class TitleActivityViewModel {

    @Inject
    SoundManager soundManager;

    private StageCountModel stageCountModel;

    private Context context;

    public TitleActivityViewModel(App app, StageCountModel stageCountModel, Context context) {
        app.getApplicationComponent().inject(this);
        this.stageCountModel = stageCountModel;
        this.context = context;
    }

    public String getDisplayStageCount() {
        return context.getString(R.string.stage_count,
                stageCountModel.getClearStageCount(),
                stageCountModel.getStageCount());
    }

    public Drawable getSoundResource() {
        boolean playable = soundManager.isPlayable();
        @DrawableRes int imageRes = playable ? R.drawable.sound_on : R.drawable.sound_off;
        return ContextCompat.getDrawable(context, imageRes);
    }
}
