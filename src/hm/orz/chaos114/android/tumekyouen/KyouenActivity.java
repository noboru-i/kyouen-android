package hm.orz.chaos114.android.tumekyouen;

import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.fragment.TumeKyouenFragment;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KyouenActivity extends FragmentActivity {
	/** 方向を表すenum */
	enum Direction {
		PREV, NEXT, NONE
	}

	/** DBアクセスオブジェクト */
	private KyouenDb kyouenDb;

	/** ステージ情報オブジェクト */
	private TumeKyouenModel stageModel;

	/** 共円描画用view */
	private TumeKyouenFragment tumeKyouenFragment;

	/** 共円描画用View */
	private OverlayView overlayView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kyouenDb = new KyouenDb(this);

		// タイトルバーを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 音量ボタンの動作変更
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Intentよりステージ情報を取得
		Intent intent = getIntent();
		stageModel = (TumeKyouenModel) intent.getSerializableExtra("item");

		setContentView(R.layout.main);

		// オーバーレイ領域の設定
		overlayView = (OverlayView) findViewById(R.id.kyouen_overlay);
		overlayView.setVisibility(View.INVISIBLE);

		// 詰め共円領域の追加
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		tumeKyouenFragment = TumeKyouenFragment.newInstance(stageModel);
		fragmentTransaction.add(R.id.fragment_container, tumeKyouenFragment);
		fragmentTransaction.commit();

		// 共円チェックボタンの設定
		Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
		kyouenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tumeKyouenFragment.getGameModel().getWhiteStoneCount() != 4) {
					// 4つの石が選択されていない場合
					new AlertDialog.Builder(KyouenActivity.this)
							.setTitle(R.string.alert_less_stone)
							.setPositiveButton("OK", null).create().show();
					return;
				}
				KyouenData data = tumeKyouenFragment.getGameModel().isKyouen();
				if (data == null) {
					// 共円でない場合
					new AlertDialog.Builder(KyouenActivity.this)
							.setTitle(R.string.alert_not_kyouen)
							.setPositiveButton("OK", null).create().show();
					// 全ての石を未選択状態に戻す
					tumeKyouenFragment.reset();
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
								showOtherStage(Direction.NONE);
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
		// プリファレンスに設定
		PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
		preferenceUtil.putInt(PreferenceUtil.KEY_LAST_STAGE_NO, stageModel.getStageNo());

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

		overlayView.setVisibility(View.INVISIBLE);
	}

	/**
	 * 共円状態を設定します。
	 */
	private void setKyouen() {
		Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
		kyouenButton.setClickable(false);
		tumeKyouenFragment.setClickable(false);

		stageModel.setClearFlag(TumeKyouenModel.CLEAR);
		kyouenDb.updateClearFlag(stageModel);
		
		// サーバに送信
		AddStageUserTask task = new AddStageUserTask();
		task.execute(stageModel);

		TextView stageNoView = (TextView) findViewById(R.id.stage_no);
		stageNoView.setTextColor(getResources().getColor(R.color.text_clear));
	}

	/**
	 * ステージの移動処理を行います。
	 * 
	 * @param direction 移動するステージの方向（PREV/NEXT）
	 * @return 移動が成功した場合true
	 */
	private boolean moveStage(final Direction direction) {
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
		case NONE:
			// 想定外の引数
			throw new IllegalArgumentException("引数がNONE");
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
					showOtherStage(direction);
				}
			});
			long maxStageNo = kyouenDb.selectMaxStageNo();
			task.execute(String.valueOf(maxStageNo));

			return false;
		}

		stageModel = newModel;
		showOtherStage(direction);
		return true;
	}

	/**
	 * {@link stageModel}のデータに合わせて画面を変更する。
	 * 
	 * @param direction 移動するステージの方向（PREV/NEXT/NONE）
	 */
	private void showOtherStage(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("引数がnull");
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		tumeKyouenFragment = TumeKyouenFragment.newInstance(stageModel);

		switch (direction) {
		case PREV:
			ft.setCustomAnimations(
					R.anim.fragment_slide_right_enter,
					R.anim.fragment_slide_right_exit);
			break;
		case NEXT:
			ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
					R.anim.fragment_slide_left_exit);
			break;
		case NONE:
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			break;
		}
		ft.replace(R.id.fragment_container, tumeKyouenFragment);
		ft.commit();

		init();
	}
	/**
	 * クリア情報を送信するタスククラス
	 * 
	 * @author ishikuranoboru
	 */
	final class AddStageUserTask extends AsyncTask<TumeKyouenModel, Void, Void> {
		@Override
		protected Void doInBackground(TumeKyouenModel... params) {
			ServerUtil.addStageUser(KyouenActivity.this, params[0]);
			return null;
		}
	};
}