package hm.orz.chaos114.android.tumekyouen;

import static hm.orz.chaos114.android.tumekyouen.constants.GcmConstants.SERVER_ID;
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

import java.io.IOException;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

/**
 * タイトル画面を表示するアクティビティ。
 * 
 * @author noboru
 */
public class TitleActivity extends FragmentActivity {
	private static final String TAG = TitleActivity.class.getSimpleName();

	public RequestToken _req = null;
	public OAuthAuthorization _oauth = null;

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

	/** twitter接続ボタン押下後の処理 */
	private View.OnClickListener mTwitterButtonListener = new View.OnClickListener() {
		/** ローディングダイアログ */
		private ProgressDialog dialog;

		@Override
		public void onClick(View v) {
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

				@Override
				protected void onPreExecute() {
					dialog = new ProgressDialog(TitleActivity.this);
					dialog.setMessage("Now Loading...");
					dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					dialog.show();
				}

				@Override
				protected void onPostExecute(Void result) {
					// twitterボタンの無効化
					setTwitterConnectButtonEnabled(false);

					dialog.dismiss();
				}

				@Override
				protected Void doInBackground(Void... params) {
					Configuration conf = ConfigurationContext.getInstance();
					_oauth = new OAuthAuthorization(conf);
					// Oauth認証オブジェクトにconsumerKeyとconsumerSecretを設定
					_oauth.setOAuthConsumer(getString(R.string.twitter_key),
							getString(R.string.twitter_secret));
					// アプリの認証オブジェクト作成
					try {
						_req = _oauth
								.getOAuthRequestToken("tumekyouen://TitleActivity");
					} catch (TwitterException e) {
						throw new RuntimeException(e);
					}
					String _uri = _req.getAuthorizationURL();
					startActivityForResult(
							new Intent(Intent.ACTION_VIEW, Uri.parse(_uri)), 0);
					return null;
				}
			};
			task.execute((Void) null);
		}
	};

	AsyncTask<Void, Void, Void> sendAllStageTask = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kyouenDb = new KyouenDb(this);

		// タイトルバーを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 音量ボタンの動作変更
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// GCMへの登録
		registGcm();

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

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent!!!");
		super.onNewIntent(intent);
		// twitter連携
		Uri uri = intent.getData();
		if (uri != null
				&& uri.toString().startsWith("tumekyouen://TitleActivity")) {
			// oauth_verifierを取得する
			final String verifier = uri.getQueryParameter("oauth_verifier");
			AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					if (verifier == null) {
						// 認証をキャンセルされた場合
						return false;
					}

					AccessToken token;
					// AccessTokenオブジェクトを取得
					try {
						Log.d(TAG, "_oauth = " + TitleActivity.this._oauth);
						Log.d(TAG, "_req = " + TitleActivity.this._req);
						Log.d(TAG, "verifier = " + verifier);
						token = TitleActivity.this._oauth.getOAuthAccessToken(
								TitleActivity.this._req, verifier);
					} catch (TwitterException e) {
						return false;
					}

					// サーバに認証情報を送信
					try {
						ServerUtil.registUser(TitleActivity.this,
								token.getToken(), token.getTokenSecret());
					} catch (IOException e) {
						return false;
					}

					// ログイン情報を保存
					LoginUtil loginUtil = new LoginUtil(TitleActivity.this);
					loginUtil.saveLoginInfo(token);
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (!result) {
						// 失敗時
						new AlertDialog.Builder(TitleActivity.this)
								.setMessage(
										R.string.alert_error_authenticate_twitter)
								.setPositiveButton(android.R.string.ok, null)
								.show();
					}
				}

			};
			task.execute((Void) null);
		}
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

		// twitter接続ボタンの設定
		Button twitterButton = (Button) findViewById(R.id.twitter_connect_button);
		twitterButton.setOnClickListener(mTwitterButtonListener);

		AsyncTask<AccessToken, Void, Boolean> task = new AsyncTask<AccessToken, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(AccessToken... params) {
				AccessToken token = params[0];

				// サーバに認証情報を送信
				try {
					ServerUtil.registUser(TitleActivity.this, token.getToken(),
							token.getTokenSecret());
				} catch (IOException e) {
					return false;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (!result) {
					// 失敗した場合
					return;
				}
				// 成功した場合
				setTwitterConnectButtonEnabled(false);
			}
		};
		LoginUtil loginUtil = new LoginUtil(this);
		AccessToken loginInfo = loginUtil.loadLoginInfo();
		if (loginInfo != null) {
			// 認証情報が存在する場合
			task.execute(loginInfo);
		}

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
	}

	/**
	 * 最後に表示していたステージ番号を返します。
	 * 
	 * @return ステージ番号
	 */
	private int getLastStageNo() {
		PreferenceUtil preferenceUtil = new PreferenceUtil(
				getApplicationContext());
		int lastStageNo = preferenceUtil
				.getInt(PreferenceUtil.KEY_LAST_STAGE_NO);
		if (lastStageNo == 0) {
			// デフォルト値を設定
			lastStageNo = 1;
		}

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
	 * Twitter接続ボタンの有効無効を変更する。
	 * 
	 * @param enabled 有効・無効フラグ
	 */
	private void setTwitterConnectButtonEnabled(boolean enabled) {
		Button button = (Button) findViewById(R.id.twitter_connect_button);
		button.setEnabled(enabled);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refresh();
	}

	@Override
	protected void onDestroy() {
		GCMRegistrar.onDestroy(this.getApplicationContext());
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("oauth", _oauth);
		outState.putSerializable("req", _req);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		_oauth = (OAuthAuthorization) savedInstanceState
				.getSerializable("oauth");
		_req = (RequestToken) savedInstanceState.getSerializable("req");
	}

	private void registGcm() {
		try {
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
		} catch (UnsupportedOperationException e) {
			Log.e("kyouen", "unsupported gcm.", e);
			return;
		}
		final String regId = GCMRegistrar.getRegistrationId(this);
		Log.i("kyouen", "regId=" + regId);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SERVER_ID);
		} else {
			final Context context = this;
			AsyncTask<Void, Void, Void> registerTask = new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					boolean registered = ServerUtil.registGcm(context, regId);
					// At this point all attempts to register with the app
					// server failed, so we need to unregister the device
					// from GCM - the app will try to register again when
					// it is restarted. Note that GCM will send an
					// unregistered callback upon completion, but
					// GCMIntentService.onUnregistered() will ignore it.
					if (!registered) {
						GCMRegistrar.unregister(context);
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
				}

			};
			// TODO 処理を元に戻す必要がある？呼び過ぎ？
			// registerTask.execute(null, null, null);
		}
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
