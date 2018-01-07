package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.model.GameModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

public class TumeKyouenView extends TableLayout {

    @Inject
    SoundManager soundManager;
    @Inject
    FirebaseAnalytics firebaseAnalytics;

    // スクリーンの幅
    private int maxScrnWidth;
    // ボタンリスト
    private List<StoneButtonView> buttons;
    // ゲーム情報保持用オブジェクト
    private GameModel gameModel;

    public TumeKyouenView(Context context) {
        this(context, null);
    }

    public TumeKyouenView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWindowSize();
        initViews();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(widthSize, widthSize);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, heightMode);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setWindowSize() {
        WindowManager manager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) {
            return;
        }
        final Display display = manager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        maxScrnWidth = displaySize.x;
    }

    public void setData(TumeKyouenModel stageModel) {
        gameModel = new GameModel(stageModel.getSize(), stageModel.getStage());
        initButtons();
        applyButtons();
    }

    private void initViews() {
        getApplicationComponent().inject(this);

        buttons = new ArrayList<>();

        // TODO create util class
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "stage");
//        bundle.putString(FirebaseAnalytics.Param.VALUE, Integer.toString(stageModel.getStageNo()));
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    private AppComponent getApplicationComponent() {
        return ((App) getContext().getApplicationContext()).getApplicationComponent();
    }

    private void initButtons() {
        for (int i = 0; i < gameModel.getSize(); i++) {
            final TableRow tableRow = new TableRow(getContext());
            addView(tableRow);
            for (int j = 0; j < gameModel.getSize(); j++) {
                final StoneButtonView button = new StoneButtonView(getContext());
                final int stoneSize = maxScrnWidth / gameModel.getSize();
                buttons.add(button);
                tableRow.addView(button, stoneSize, stoneSize);

                button.setOnClickListener((v) -> {
                    final StoneButtonView b = (StoneButtonView) v;
                    final int index = buttons.indexOf(b);

                    final int col = index % gameModel.getSize();
                    final int row = index / gameModel.getSize();
                    if (!gameModel.hasStone(col, row)) {
                        return;
                    }

                    gameModel.switchColor(col, row);
                    soundManager.play(R.raw.se_maoudamashii_se_finger01);

                    applyButtons();
                });
            }
        }
    }

    private void applyButtons() {
        final int size = gameModel.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                final StoneButtonView button = buttons.get(row * size + col);
                if (gameModel.hasStone(col, row)) {
                    if (gameModel.isSelected(col, row)) {
                        button.setState(StoneButtonView.ButtonState.WHITE);
                    } else {
                        button.setState(StoneButtonView.ButtonState.BLACK);
                    }
                }
            }
        }
    }

    /**
     * 盤の状態を初期状態に戻す。
     */
    void reset() {
        gameModel.reset();
        applyButtons();
    }

    @Override
    public void setClickable(final boolean clickable) {
        for (final View v : buttons) {
            v.setClickable(clickable);
        }
    }

    /**
     * ゲーム情報保持用オブジェクトを返す。
     *
     * @return ゲーム情報保持用オブジェクト
     */
    GameModel getGameModel() {
        return gameModel;
    }
}
