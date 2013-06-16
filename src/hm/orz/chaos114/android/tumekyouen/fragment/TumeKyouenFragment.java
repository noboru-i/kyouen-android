package hm.orz.chaos114.android.tumekyouen.fragment;

import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.model.GameModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

public class TumeKyouenFragment extends Fragment {
	/** ボタン色を表すenum */
	enum ButtonState {
		NONE, BLACK, WHITE,
	}

	Context context;
	/** メインのレイアウト */
	private TableLayout layout;

	/** スクリーンの幅 */
	private int maxScrnWidth;

	/** 背景描画用Bitmap */
	private Bitmap background;

	/** ボタンリスト */
	private List<Button> buttons;

	/** ゲーム情報保持用オブジェクト */
	private GameModel gameModel;

	public static TumeKyouenFragment newInstance(final TumeKyouenModel stageModel) {
		final TumeKyouenFragment tumeKyouenFragment = new TumeKyouenFragment();
		final Bundle bundle = new Bundle();
		bundle.putSerializable("stage", stageModel);
		tumeKyouenFragment.setArguments(bundle);

		return tumeKyouenFragment;
	}

	/**
	 * デフォルトコンストラクタ。
	 */
	public TumeKyouenFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();

		final TumeKyouenModel stageModel = (TumeKyouenModel) getArguments()
				.getSerializable("stage");
		gameModel = new GameModel(stageModel.getSize(), stageModel.getStage());

		// ディスプレイサイズの取得
		final Display disp = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		maxScrnWidth = disp.getWidth();

		layout = new TableLayout(context);
		buttons = new ArrayList<Button>();

		init();

		return layout;
	}

	private void init() {
		for (int i = 0; i < gameModel.getSize(); i++) {
			final TableRow row = new TableRow(context);
			layout.addView(row);
			for (int j = 0; j < gameModel.getSize(); j++) {
				final Button button = new Button(context);
				button.setBackgroundDrawable(new BitmapDrawable(
						createBackgroundBitmap()));
				final int stoneSize = maxScrnWidth / gameModel.getSize();
				button.setWidth(stoneSize);
				button.setHeight(stoneSize);
				if (gameModel.hasStone(i, j)) {
					button.setTag(ButtonState.BLACK);
					setBlack(button);
				} else {
					button.setTag(ButtonState.NONE);
				}
				buttons.add(button);
				row.addView(button, stoneSize, stoneSize);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						final int index = buttons.indexOf(v);
						final Button button = (Button) v;
						if (v.getTag() == ButtonState.NONE) {
							// 石が設定されていない場合
							return;
						}

						// 効果音の再生
						SoundManager.getInstance(context).play(
								R.raw.se_maoudamashii_se_finger01);

						// 色の反転
						final int col = index % gameModel.getSize();
						final int row = index / gameModel.getSize();
						switchStoneColor(button);
						gameModel.switchColor(col, row);
					}
				});
			}
		}
	}

	/**
	 * 盤の状態を初期状態に戻す。
	 */
	public void reset() {
		for (int i = 0; i < buttons.size(); i++) {
			final Button button = buttons.get(i);
			if (button.getTag() == ButtonState.WHITE) {
				// 色の反転
				final int col = i % gameModel.getSize();
				final int row = i / gameModel.getSize();
				switchStoneColor(button);
				gameModel.switchColor(col, row);
			}
		}
	}

	public void setClickable(final boolean clickable) {
		for (final Button b : buttons) {
			b.setClickable(clickable);
		}
	}

	/**
	 * 背景に表示するBitmapを返す。<br>
	 * インスタンス変数に保持されていない場合、作成し、返す。
	 *
	 * @return 背景用Bitmap
	 */
	private Bitmap createBackgroundBitmap() {
		if (background == null) {
			resetBackgroundBitmap();
		}

		return background;
	}

	/**
	 * 背景に表示するbitmapを作成する。
	 */
	private void resetBackgroundBitmap() {
		final int bitmapSize = maxScrnWidth / gameModel.getSize();
		final Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);

		final Paint paint = new Paint();
		paint.setColor(Color.rgb(128, 128, 128));
		paint.setStrokeWidth(2);

		canvas.drawLine(bitmapSize / 2, 0, bitmapSize / 2, bitmapSize, paint);
		canvas.drawLine(0, bitmapSize / 2, bitmapSize, bitmapSize / 2, paint);

		paint.setColor(Color.rgb(32, 32, 32));
		paint.setStrokeWidth(1);

		canvas.drawLine(bitmapSize / 2, 0, bitmapSize / 2, bitmapSize, paint);
		canvas.drawLine(0, bitmapSize / 2, bitmapSize, bitmapSize / 2, paint);
		background = bitmap;
	}

	private void setBlack(final Button b) {
		b.setBackgroundResource(R.drawable.circle_black);
	}

	private void setWhite(final Button b) {
		b.setBackgroundResource(R.drawable.circle_white);
	}

	/**
	 * 石の色を変更する。 <br>
	 * 白を黒に、黒を白に変更する。
	 *
	 * @param b 変更するボタン
	 */
	private void switchStoneColor(final Button b) {
		final ButtonState state = (ButtonState) b.getTag();
		switch (state) {
		case WHITE:
			setBlack(b);
			b.setTag(ButtonState.BLACK);
			break;
		case BLACK:
			setWhite(b);
			b.setTag(ButtonState.WHITE);
			break;
		default:
			throw new IllegalArgumentException();
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
