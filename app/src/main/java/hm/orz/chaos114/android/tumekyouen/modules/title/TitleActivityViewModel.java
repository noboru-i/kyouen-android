package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.content.Context;
import android.graphics.drawable.Drawable;

import javax.inject.Inject;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

/**
 * TitleActivity用のViewModel。
 */
public final class TitleActivityViewModel {

    private final Context context;
    private final StageCountModel stageCountModel;
    private final SoundManager soundManager;

    TitleActivityViewModel(Context context, StageCountModel stageCountModel, SoundManager soundManager) {
        this.context = context;
        this.stageCountModel = stageCountModel;
        this.soundManager = soundManager;
    }

    public String getDisplayStageCount() {
        return context.getString(R.string.stage_count,
                stageCountModel.clearStageCount(),
                stageCountModel.stageCount());
    }

    public Drawable getSoundResource() {
        boolean playable = soundManager.isPlayable();
        @DrawableRes int imageRes = playable ? R.drawable.sound_on : R.drawable.sound_off;
        return ContextCompat.getDrawable(context, imageRes);
    }
}
