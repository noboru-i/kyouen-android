package hm.orz.chaos114.android.tumekyouen;

import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.model.GameModel;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class KyouenActivity extends Activity {

	/** ボタン色を表すenum */
	enum ButtonState {
		NONE, BLACK, WHITE,
	}

	/** 方向を表すenum */
	enum Direction {
		PREV, NEXT
	}

	/** DBアクセスオブジェクト */
	private KyouenDb kyouenDb;

	/** ボタンリスト */
	private List<Button> buttons;

	/** スクリーンの幅 */
	private int maxScrnWidth;

	/** 背景描画用Bitmap */
	private Bitmap background;

	/** ゲーム情報保持用オブジェクト */
	private GameModel gameModel;

	/** ステージ情報オブジェクト */
	private TumeKyouenModel stageModel;

	/** 共円描画用View */
	private OverlayView overlayView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kyouenDb = new KyouenDb(this);

		// タイトルバーを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		hideStatusBar();

		// 音量ボタンの動作変更
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Intentよりステージ情報を取得
		Intent intent = getIntent();
		stageModel = (TumeKyouenModel) intent.getSerializableExtra("item");

		setContentView(R.layout.main);

		// ディスプレイサイズの取得
		Display disp = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		maxScrnWidth = disp.getWidth();

		// 広告の設定
		AdUtil.addAdView(this, (LinearLayout) findViewById(R.id.ad_layout));

		// オーバーレイ領域の設定
		overlayView = new OverlayView(this, maxScrnWidth);
		overlayView.setVisibility(View.INVISIBLE);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
		frameLayout.addView(overlayView, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, maxScrnWidth));

		// チェックボタンの設定
		Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
		kyouenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gameModel.getWhiteStoneCount() != 4) {
					// 4つの石が選択されていない場合
					new AlertDialog.Builder(KyouenActivity.this)
							.setTitle(R.string.alert_less_stone)
							.setPositiveButton("OK", null).create().show();
					return;
				}
				KyouenData data = gameModel.isKyouen();
				if (data == null) {
					// 共円でない場合
					new AlertDialog.Builder(KyouenActivity.this)
							.setTitle(R.string.alert_not_kyouen)
							.setPositiveButton("OK", null).create().show();
					// 全ての石を未選択状態に変更
					for (int i = 0; i < buttons.size(); i++) {
						Button button = buttons.get(i);
						if (button.getTag() == ButtonState.WHITE) {
							// 色の反転
							int col = i % gameModel.getSize();
							int row = i / gameModel.getSize();
							switchStoneColor(button);
							gameModel.switchColor(col, row);
						}
					}
					return;
				}

				// 共円の場合
				SoundManager.getInstance(KyouenActivity.this).play(
						R.raw.se_maoudamashii_onepoint23);
				new AlertDialog.Builder(KyouenActivity.this)
						.setTitle(R.string.kyouen)
						.setPositiveButton("OK", null)
						.setNeutralButton("Next",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										moveStage(Direction.NEXT);
									}
								}).create().show();
				overlayView.setData(stageModel.getSize(), data);
				overlayView.setVisibility(View.VISIBLE);
				setKyouen();
			}
		});

		// prev,nextボタンの設定
		final Button prevButton = (Button) findViewById(R.id.prev_button);
		final Button nextButton = (Button) findViewById(R.id.next_button);
		View.OnClickListener stageChangeListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Direction direction = null;
				if (v == prevButton) {
					// prevボタン押下時
					direction = Direction.PREV;
				} else if (v == nextButton) {
					// nextボタン押下時
					direction = Direction.NEXT;
				}

				moveStage(direction);
			}
		};
		prevButton.setOnClickListener(stageChangeListener);
		nextButton.setOnClickListener(stageChangeListener);

		LinearLayout stageNoLayout = (LinearLayout) findViewById(R.id.stage_no_layout);
		stageNoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StageSelectDialog dialog = new StageSelectDialog(
						KyouenActivity.this,
						new StageSelectDialog.OnSuccessListener() {
							@Override
							public void onSuccess(int count) {
								long maxStageNo = kyouenDb.selectMaxStageNo();
								int nextStageNo = count;
								if (nextStageNo > maxStageNo
										|| nextStageNo == -1) {
									nextStageNo = (int) maxStageNo;
								}

								// ダイアログで選択されたステージを表示
								TumeKyouenModel newModel = kyouenDb
										.selectCurrentStage(nextStageNo);
								stageModel = newModel;
								init();
							}
						}, null);
				dialog.setStageNo(stageModel.getStageNo());
				dialog.show();
			}
		});

		// 初期化
		init();
	}

	private void init() {
		gameModel = new GameModel(stageModel.getSize(), stageModel.getStage());
		buttons = new ArrayList<Button>();

		// プリファレンスに設定
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = sp.edit();
		editor.putInt("last_stage_no", stageModel.getStageNo());
		editor.commit();

		// ステージ名表示領域の設定
		TextView stageNoView = (TextView) findViewById(R.id.stage_no);
		if (stageModel.getClearFlag() == TumeKyouenModel.CLEAR) {
			// クリア後の場合
			stageNoView.setTextColor(getResources()
					.getColor(R.color.text_clear));
		} else {
			// 未クリアの場合
			stageNoView.setTextColor(getResources().getColor(
					R.color.text_not_clear));
		}
		stageNoView.setText("STAGE:" + stageModel.getStageNo());

		// ステージ作者領域の設定
		TextView stageCreatorView = (TextView) findViewById(R.id.stage_creator);
		stageCreatorView.setText("created by " + stageModel.getCreator());

		// prev,nextボタンの設定
		Button prevButton = (Button) findViewById(R.id.prev_button);
		if (stageModel.getStageNo() == 1) {
			// 先頭ステージの場合は押下不可
			prevButton.setClickable(false);
		} else {
			prevButton.setClickable(true);
		}

		// 共円ボタンの設定
		Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
		kyouenButton.setClickable(true);

		// ボタン表示領域の設定
		resetBackgroundBitmap();
		TableLayout layout = (TableLayout) findViewById(R.id.layout);
		layout.removeAllViews();
		TableRow row = null;
		for (int i = 0; i < gameModel.getSize(); i++) {
			row = new TableRow(this);
			layout.addView(row);
			for (int j = 0; j < gameModel.getSize(); j++) {
				Button button = new Button(this);
				button.setBackgroundDrawable(new BitmapDrawable(
						createBackgroundBitmap()));
				button.setWidth(maxScrnWidth / gameModel.getSize());
				button.setHeight(maxScrnWidth / gameModel.getSize());
				if (gameModel.hasStone(i, j)) {
					button.setTag(ButtonState.BLACK);
					setBlack(button);
				} else {
					button.setTag(ButtonState.NONE);
				}
				buttons.add(button);
				row.addView(button);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int index = buttons.indexOf(v);
						Button button = (Button) v;
						if (v.getTag() == ButtonState.NONE) {
							// 石が設定されていない場合
							return;
						}

						// 効果音の再生
						SoundManager.getInstance(KyouenActivity.this).play(
								R.raw.se_maoudamashii_se_finger01);

						// 色の反転
						int col = index % gameModel.getSize();
						int row = index / gameModel.getSize();
						switchStoneColor(button);
						gameModel.switchColor(col, row);
					}
				});
			}
		}

		overlayView.setVisibility(View.INVISIBLE);
	}

	/**
	 * 共円状態を設定します。
	 */
	private void setKyouen() {
		Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
		kyouenButton.setClickable(false);
		for (Button b : buttons) {
			b.setClickable(false);
		}

		stageModel.setClearFlag(TumeKyouenModel.CLEAR);
		kyouenDb.updateClearFlag(stageModel);

		TextView stageNoView = (TextView) findViewById(R.id.stage_no);
		stageNoView.setTextColor(getResources().getColor(R.color.text_clear));
	}

	/**
	 * ステータスバーを非表示状態にします。
	 */
	private void hideStatusBar() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * 石の色を変更する。 <br>
	 * 白を黒に、黒を白に変更する。
	 * 
	 * @param b 変更するボタン
	 */
	private void switchStoneColor(Button b) {
		ButtonState state = (ButtonState) b.getTag();
		switch (state) {
		case WHITE:
			setBlack(b);
			b.setTag(ButtonState.BLACK);
			break;
		case BLACK:
			setWhite(b);
			b.setTag(ButtonState.WHITE);
			break;
		}
	}

	private void setBlack(Button b) {
		b.setBackgroundResource(R.drawable.circle_black);
	}

	private void setWhite(Button b) {
		b.setBackgroundResource(R.drawable.circle_white);
	}

	/**
	 * 背景に表示するBitmapを返します。<br>
	 * インスタンス変数に保持されていない場合、作成し、返却します。
	 * 
	 * @return 背景用Bitmap
	 */
	private Bitmap createBackgroundBitmap() {
		if (background == null) {
			resetBackgroundBitmap();
		}

		return background;
	}

	private void resetBackgroundBitmap() {
		int bitmapSize = maxScrnWidth / gameModel.getSize();
		Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint paint = new Paint();
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

	/**
	 * ステージの移動処理を行います。
	 * 
	 * @param direction 移動するステージの方向（PREV/NEXT）
	 * @return 移動が成功した場合true
	 */
	private boolean moveStage(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("引数がnull");
		}

		TumeKyouenModel newModel = null;
		switch (direction) {
		case PREV:
			// prev選択時
			newModel = kyouenDb.selectPrevStage(stageModel.getStageNo());
			break;
		case NEXT:
			// next選択時
			newModel = kyouenDb.selectNextStage(stageModel.getStageNo());
			break;
		}

		if (newModel == null) {
			// 次のステージが存在しない場合、WEBより取得する
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle("通信中");
			dialog.setMessage("Loading...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();

			InsertDataTask task = new InsertDataTask(this, new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();

					TumeKyouenModel newModel = kyouenDb
							.selectNextStage(stageModel.getStageNo());
					if (newModel == null) {
						// WEBより取得後も取得できない場合
						return;
					}

					stageModel = newModel;
					init();
				}
			});
			long maxStageNo = kyouenDb.selectMaxStageNo();
			task.execute(String.valueOf(maxStageNo));

			return false;
		}

		stageModel = newModel;
		init();
		return true;
	}

}