package hm.orz.chaos114.android.tumekyouen.modules.create;

import android.content.Context;
import android.util.AttributeSet;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.Point;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.modules.common.KyouenView;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.StoneButtonView;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import timber.log.Timber;

public class CreateKyouenView extends KyouenView {

    public interface CreateKyouenViewListener {
        void onKyouen(KyouenData kyouenData);
    }

    private SoundManager soundManager;
    private CreateKyouenViewListener listener;

    public CreateKyouenView(Context context) {
        this(context, null);
    }

    public CreateKyouenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void inject(SoundManager soundManager, CreateKyouenViewListener listener) {
        this.soundManager = soundManager;
        this.listener = listener;
    }

    @Override
    public void setData(TumeKyouenModel stageModel) {
        super.setData(stageModel);
    }

    @Override
    protected void onClickButton(StoneButtonView b) {
        final int index = buttons.indexOf(b);

        final int col = index % gameModel.size();
        final int row = index / gameModel.size();
        gameModel.putStone(col, row);
        soundManager.play(R.raw.se_maoudamashii_se_finger01);

        applyButtons();

        if (gameModel.getBlackStoneCount() >= 4) {
            KyouenData kyouenData = gameModel.hasKyouen();
            Timber.d("kyouenData = %s", kyouenData);
            if (kyouenData != null) {
                applyWhiteStones(kyouenData);
                listener.onKyouen(kyouenData);
            }
        }
    }

    public void popStone() {
        gameModel.popStone();
        applyBlackStones();
    }

    private void applyWhiteStones(KyouenData kyouenData) {
        for (Point point : kyouenData.points()) {
            gameModel.switchColor((int) point.x(), (int) point.y());
        }
        applyButtons();
    }

    private void applyBlackStones() {
        gameModel.reset();
        applyButtons();
    }
}
