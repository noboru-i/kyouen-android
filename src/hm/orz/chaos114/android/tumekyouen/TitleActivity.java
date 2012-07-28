package hm.orz.chaos114.android.tumekyouen;

import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * タイトル画面を表示するアクティビティ。
 * 
 * @author noboru
 */
public class TitleActivity extends Activity {

	/** DBオブジェクト */
	private KyouenDb kyouenDb;

	/** 取得ボタン押下後の処理 */
	private StageGetDialog.OnSuccessListener mSuccessListener = new StageGetDialog.OnSuccessListener() {
		@Override
		public void onSuccess(int count) {
			int taskCount;
			if (count == -1) {
				// 全件の場合
				taskCount = Integer.MAX_VALUE;
			} else {
				taskCount = count;
			}
			InsertDataTask task = new InsertDataTask(TitleActivity.this,
					taskCount, new Runnable() {
						@Override
						public void run() {
							refresh();
						}
					});
			long maxStageNo = kyouenDb.selectMaxStageNo();
			task.execute(String.valueOf(maxStageNo));
		}
	};

	/** キャンセルボタン押下後の処理 */
	private DialogInterface.OnCancelListener mCancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			refresh();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kyouenDb = new KyouenDb(this);

		// タイトルバーを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		hideStatusBar();

		// 音量ボタンの動作変更
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (!hasStageData()) {
			// データが存在しない場合
			// ローディングを表示
			setContentView(R.layout.loading);
			InitDataTask task = new InitDataTask();
			task.execute((Void) null);
		} else {
			// タイトルを表示
			initTitle();
		}
	}

	/**
	 * DB上のデータが存在するかをチェックします。
	 * 
	 * @return データが存在する場合、true
	 */
	private boolean hasStageData() {
		long count = kyouenDb.selectMaxStageNo();
		if (count == 0) {
			return false;
		}
		return true;
	}

	/**
	 * タイトル画面を初期化する。
	 */
	void initTitle() {
		// タイトル画面を設定
		setContentView(R.layout.title);

		// パズルボタンの設定
		Button puzzleButton = (Button) findViewById(R.id.start_puzzle_button);
		puzzleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int stageNo = getLastStageNo();
				TumeKyouenModel item = kyouenDb.selectCurrentStage(stageNo);
				Intent intent = new Intent(TitleActivity.this,
						KyouenActivity.class);
				intent.putExtra("item", item);
				startActivityForResult(intent, 0);
			}
		});

		// ステージ取得ボタンの設定
		Button getStageButton = (Button) findViewById(R.id.get_stage_button);
		getStageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Button button = (Button) v;
				button.setClickable(false);
				button.setText(TitleActivity.this
						.getString(R.string.get_more_loading));

				StageGetDialog dialog = new StageGetDialog(TitleActivity.this,
						mSuccessListener, mCancelListener);
				dialog.show();
			}
		});

		// ステージ作成ボタンの設定
		Button createStageButton = (Button) findViewById(R.id.create_stage_button);
		createStageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean hasKyouenChecker = false;
				try {
					PackageManager pm = getPackageManager();
					pm.getApplicationInfo(
							"hm.orz.chaos114.android.kyouenchecker", 0);
					hasKyouenChecker = true;
				} catch (NameNotFoundException e) {
				}
				if (hasKyouenChecker) {
					Intent intent = new Intent();
					intent.setClassName(
							"hm.orz.chaos114.android.kyouenchecker",
							"hm.orz.chaos114.android.kyouenchecker.KyouenActivity");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} else {
					new AlertDialog.Builder(TitleActivity.this)
							.setMessage(R.string.alert_install_kyouenchecker)
							.setPositiveButton("YES",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Uri uri = Uri
													.parse("market://details?id=hm.orz.chaos114.android.kyouenchecker");
											Intent intent = new Intent(
													Intent.ACTION_VIEW, uri);
											startActivity(intent);
										}
									}).setNegativeButton("NO", null).show();
				}
			}
		});

		// 音量領域の設定
		ImageView soundImageView = (ImageView) findViewById(R.id.sound_button);
		soundImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SoundManager.getInstance(TitleActivity.this).switchPlayable();
				refreshSoundState();
			}
		});

		// 描画内容を更新
		refresh();

		// 広告の表示
		AdUtil.addAdView(this, (LinearLayout) findViewById(R.id.ad_layout));
	}

	/**
	 * 最後に表示していたステージ番号を返します。
	 * 
	 * @return ステージ番号
	 */
	private int getLastStageNo() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		int lastStageNo = sp.getInt("last_stage_no", 1);

		return lastStageNo;
	}

	/**
	 * 描画内容を再設定します。
	 */
	private void refresh() {
		refreshGetStageButton();
		refreshStageCount();
		refreshSoundState();
	}

	/**
	 * ステージ取得ボタンを再設定します。
	 */
	private void refreshGetStageButton() {
		Button getStageButton = (Button) findViewById(R.id.get_stage_button);
		if (InsertDataTask.isRunning()) {
			getStageButton.setClickable(false);
			getStageButton.setText(TitleActivity.this
					.getString(R.string.get_more_loading));
		} else {
			getStageButton.setClickable(true);
			getStageButton.setText(TitleActivity.this
					.getString(R.string.get_more));
		}
	}

	/**
	 * ステージ数領域を再設定します。
	 */
	private void refreshStageCount() {
		TextView stageCountView = (TextView) findViewById(R.id.stage_count);
		StageCountModel stageCountModel = kyouenDb.selectStageCount();
		stageCountView.setText(stageCountModel.getClearStageCount() + " / "
				+ stageCountModel.getStageCount());
	}

	/**
	 * 音量領域を再設定します。
	 */
	private void refreshSoundState() {
		ImageView soundImageView = (ImageView) findViewById(R.id.sound_button);
		if (SoundManager.getInstance(this).isPlayable()) {
			soundImageView.setImageResource(R.drawable.sound_on);
		} else {
			soundImageView.setImageResource(R.drawable.sound_off);
		}
	}

	/**
	 * ステータスバーを非表示状態にします。
	 */
	private void hideStatusBar() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refresh();
	}

	/**
	 * 初期データ登録用のタスク。
	 * 
	 * @author noboru
	 */
	class InitDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String[] initData = new String[] {
					"1,6,000000010000001100001100000000001000,noboru",
					"2,6,000000000000000100010010001100000000,noboru",
					"3,6,000000001000010000000100010010001000,noboru",
					"4,6,001000001000000010010000010100000000,noboru",
					"5,6,000000001011010000000010001000000010,noboru",
					"6,6,000100000000101011010000000000000000,noboru",
					"7,6,000000001010000000010010000000001010,noboru",
					"8,6,001000000001010000010010000001000000,noboru",
					"9,6,000000001000010000000010000100001000,noboru",
					"10,6,000100000010010000000100000010010000,noboru" };
			for (String data : initData) {
				kyouenDb.insert(data);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			initTitle();
		}
	}
}
