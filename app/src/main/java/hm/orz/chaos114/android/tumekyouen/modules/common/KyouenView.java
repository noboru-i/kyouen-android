package hm.orz.chaos114.android.tumekyouen.modules.common;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.tumekyouen.model.GameModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.StoneButtonView;

public class KyouenView extends TableLayout {

    // スクリーンの幅
    private int maxScrnWidth;
    // ボタンリスト
    protected List<StoneButtonView> buttons;
    // ゲーム情報保持用オブジェクト
    protected GameModel gameModel;

    public KyouenView(Context context) {
        this(context, null);
    }

    public KyouenView(Context context, AttributeSet attrs) {
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
        gameModel = GameModel.create(stageModel.size(), stageModel.stage());
        initButtons();
        applyButtons();
    }

    protected void onClickButton(StoneButtonView b) {
        // no-op
    }

    private void initViews() {
        buttons = new ArrayList<>();
    }

    private void initButtons() {
        for (int i = 0; i < gameModel.size(); i++) {
            final TableRow tableRow = new TableRow(getContext());
            addView(tableRow);
            for (int j = 0; j < gameModel.size(); j++) {
                final StoneButtonView button = new StoneButtonView(getContext());
                final int stoneSize = maxScrnWidth / gameModel.size();
                buttons.add(button);
                tableRow.addView(button, stoneSize, stoneSize);

                button.setOnClickListener((v) -> {
                    onClickButton((StoneButtonView) v);
                });
            }
        }
    }

    protected void applyButtons() {
        final int size = gameModel.size();
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
    public void reset() {
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
    public GameModel getGameModel() {
        return gameModel;
    }
}
