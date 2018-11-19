package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.google.firebase.analytics.FirebaseAnalytics;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.modules.common.KyouenView;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

public class TumeKyouenView extends KyouenView {

    private SoundManager soundManager;
    private FirebaseAnalytics firebaseAnalytics;

    public TumeKyouenView(Context context) {
        this(context, null);
    }

    public TumeKyouenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void inject(SoundManager soundManager, FirebaseAnalytics firebaseAnalytics) {
        this.soundManager = soundManager;
        this.firebaseAnalytics = firebaseAnalytics;
    }

    public void setData(TumeKyouenModel stageModel) {
        super.setData(stageModel);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "stage");
        bundle.putString(FirebaseAnalytics.Param.VALUE, Integer.toString(stageModel.stageNo()));
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    protected void onClickButton(StoneButtonView b) {
        final int index = buttons.indexOf(b);

        final int col = index % gameModel.size();
        final int row = index / gameModel.size();
        if (!gameModel.hasStone(col, row)) {
            return;
        }

        gameModel.switchColor(col, row);
        soundManager.play(R.raw.se_maoudamashii_se_finger01);

        applyButtons();
    }
}
