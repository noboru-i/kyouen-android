package hm.orz.chaos114.android.tumekyouen;

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
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

/**
 * タイトル画面を表示するアクティビティ。
 */
public class TitleActivity extends FragmentActivity {
    private static final String TAG = TitleActivity.class.getSimpleName();

    RequestToken req;
    OAuthAuthorization oauth;

    @Bind(R.id.get_stage_button)
    Button mGetStageButton;

    @Bind(R.id.connect_button)
    Button mConnectButton;

    @Bind(R.id.sync_button)
    Button mSyncButton;

    @Bind(R.id.sound_button)
    ImageView mSoundImageView;

    @Bind(R.id.stage_count)
    TextView stageCountView;

    /** DBオブジェクト */
    private KyouenDb kyouenDb;

    /** 取得ボタン押下後の処理 */
    private final StageGetDialog.OnSuccessListener mSuccessListener = new StageGetDialog.OnSuccessListener() {
        @Override
        public void onSuccess(final int count) {
            int taskCount;
            if (count == -1) {
                // 全件の場合
                taskCount = Integer.MAX_VALUE;
            } else {
                taskCount = count;
            }
            final InsertDataTask task = new InsertDataTask(TitleActivity.this,
                    taskCount, new Runnable() {
                @Override
                public void run() {
                    refreshAll();
                }
            });
            final long maxStageNo = kyouenDb.selectMaxStageNo();
            task.execute(String.valueOf(maxStageNo));
        }
    };

    /** キャンセルボタン押下後の処理 */
    private final DialogInterface.OnCancelListener mCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(final DialogInterface dialog) {
            refreshAll();
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kyouenDb = new KyouenDb(this);

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // GCMへの登録
        registGcm();

        if (kyouenDb.selectMaxStageNo() == 0) {
            // データが存在しない場合
            // ローディングを表示
            setContentView(R.layout.loading);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    inserInitialDatatInBackground();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    showTitle();
                }
            }.execute();
        } else {
            // タイトルを表示
            showTitle();
        }

        // 広告の表示
        AdView mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final Uri uri = intent.getData();
        if (uri != null
                && uri.toString().startsWith("tumekyouen://TitleActivity")) {
            // twitter連携

            // oauth_verifierを取得する
            String verifier = uri.getQueryParameter("oauth_verifier");
            new AsyncTask<String, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(String... strings) {
                    return authTwitterInBackground(strings[0]);
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    if (aBoolean) {
                        onSuccessTwitterAuth();
                    } else {
                        onFailedTwitterAuth();
                    }
                }
            }.execute(verifier);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        refreshAll();
    }

    @Override
    protected void onDestroy() {
        GCMRegistrar.onDestroy(getApplicationContext());
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("oauth", oauth);
        outState.putSerializable("req", req);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        oauth = (OAuthAuthorization) savedInstanceState
                .getSerializable("oauth");
        req = (RequestToken) savedInstanceState.getSerializable("req");
    }

    /**
     * スタートボタンの設定
     */
    @OnClick(R.id.start_puzzle_button)
    public void onClickStartButton() {
        Log.d(TAG, "onClickStartButton!!!!!!");
        final int stageNo = getLastStageNo();
        final TumeKyouenModel item = kyouenDb.selectCurrentStage(stageNo);
        KyouenActivity.start(this, item);
    }

    /**
     * ステージ取得ボタンの設定
     */
    @OnClick(R.id.get_stage_button)
    void onClickGetStage() {
        mGetStageButton.setClickable(false);
        mGetStageButton.setText(getString(R.string.get_more_loading));

        final StageGetDialog dialog = new StageGetDialog(this,
                mSuccessListener, mCancelListener);
        dialog.show();
    }

    /**
     * ステージ作成ボタン押下時の処理
     */
    @OnClick(R.id.create_stage_button)
    void onClickCreateStage() {
        boolean hasKyouenChecker;
        try {
            // 共円チェッカーの存在有無チェック
            final PackageManager pm = getPackageManager();
            pm.getApplicationInfo("hm.orz.chaos114.android.kyouenchecker", 0);
            hasKyouenChecker = true;
        } catch (final NameNotFoundException e) {
            // 存在しない場合
            hasKyouenChecker = false;
        }
        if (hasKyouenChecker) {
            // 共円チェッカーの起動
            final Intent intent = new Intent();
            intent.setClassName("hm.orz.chaos114.android.kyouenchecker",
                    "hm.orz.chaos114.android.kyouenchecker.KyouenActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // マーケットへの導線を表示
            new AlertDialog.Builder(this)
                    .setMessage(R.string.alert_install_kyouenchecker)
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int which) {
                                    // マーケットを開く
                                    final Uri uri = Uri
                                            .parse("market://details?id=hm.orz.chaos114.android.kyouenchecker");
                                    final Intent intent = new Intent(
                                            Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            }).setNegativeButton("NO", null).show();
        }
    }

    /** twitter接続ボタン押下後の処理 */
    @OnClick(R.id.connect_button)
    void onClickConnectButton() {
        final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                // ローディングダイアログの表示
                dialog = new ProgressDialog(TitleActivity.this);
                dialog.setMessage("Now Loading...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(final Void... params) {
                final Configuration conf = ConfigurationContext.getInstance();
                oauth = new OAuthAuthorization(conf);
                // Oauth認証オブジェクトにconsumerKeyとconsumerSecretを設定
                oauth.setOAuthConsumer(getString(R.string.twitter_key),
                        getString(R.string.twitter_secret));
                // アプリの認証オブジェクト作成
                try {
                    req = oauth
                            .getOAuthRequestToken("tumekyouen://TitleActivity");
                } catch (final TwitterException e) {
                    Log.e(TAG, "TwitterException", e);
                    return false;
                }
                final String uri = req.getAuthorizationURL();
                startActivityForResult(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(uri)), 0);
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                if (!result) {
                    new AlertDialog.Builder(TitleActivity.this)
                            .setMessage(
                                    R.string.alert_error_authenticate_twitter)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                dialog.dismiss();
            }
        };
        task.execute((Void) null);
    }

    /**
     * クリア情報を同期ボタン押下時の処理
     */
    @OnClick(R.id.sync_button)
    void onClickSyncButton() {
        // ボタンを無効化
        mSyncButton.setEnabled(false);

        // クリア情報を同期
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                syncClearDataInBackground();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                enableSyncButton();
            }
        }.execute();

    }

    /**
     * 音量領域の設定
     */
    @OnClick(R.id.sound_button)
    void changeSound() {
        SoundManager.getInstance(this).switchPlayable();
        refreshSoundState();
    }

    /**
     * クリアステージデータの同期を行う。
     */
    @WorkerThread
    void syncClearDataInBackground() {
        // クリアした情報を取得
        final List<TumeKyouenModel> stages = kyouenDb.selectAllClearStage();
        // ステージデータを送信
        final List<TumeKyouenModel> clearList = ServerUtil.addAllStageUser(
                this, stages);
        if (clearList != null) {
            kyouenDb.updateSyncClearData(clearList);
        }
    }

    @WorkerThread
    boolean authTwitterInBackground(final String verifier) {
        if (verifier == null) {
            // 認証をキャンセルされた場合
            return false;
        }

        AccessToken token;
        // AccessTokenオブジェクトを取得
        try {
            Log.d(TAG, "oauth = " + oauth);
            Log.d(TAG, "req = " + req);
            Log.d(TAG, "verifier = " + verifier);
            token = oauth.getOAuthAccessToken(req, verifier);
        } catch (final TwitterException e) {
            return false;
        }

        // サーバに認証情報を送信
        try {
            ServerUtil.registUser(this, token.getToken(),
                    token.getTokenSecret());
        } catch (final IOException e) {
            return false;
        }

        // ログイン情報を保存
        final LoginUtil loginUtil = new LoginUtil(this);
        loginUtil.saveLoginInfo(token);

        return true;
    }

    /**
     * 初期データを登録する。
     */
    @WorkerThread
    void inserInitialDatatInBackground() {
        final String[] initData = new String[]{
                "1,6,000000010000001100001100000000001000,noboru",
                "2,6,000000000000000100010010001100000000,noboru",
                "3,6,000000001000010000000100010010001000,noboru",
                "4,6,001000001000000010010000010100000000,noboru",
                "5,6,000000001011010000000010001000000010,noboru",
                "6,6,000100000000101011010000000000000000,noboru",
                "7,6,000000001010000000010010000000001010,noboru",
                "8,6,001000000001010000010010000001000000,noboru",
                "9,6,000000001000010000000010000100001000,noboru",
                "10,6,000100000010010000000100000010010000,noboru"};
        for (final String data : initData) {
            kyouenDb.insert(data);
        }
    }

    /**
     * タイトル画面を初期化する。
     */
    @MainThread
    void showTitle() {
        // タイトル画面を設定
        setContentView(R.layout.title);
        ButterKnife.bind(this);

        final LoginUtil loginUtil = new LoginUtil(this);
        final AccessToken loginInfo = loginUtil.loadLoginInfo();
        if (loginInfo != null) {
            // 認証情報が存在する場合
            final ServerRegistTask task = new ServerRegistTask();
            task.execute(loginInfo);
        }

        // 描画内容を更新
        refreshAll();
    }

    /**
     * twitter連携に成功した場合の処理。
     * ボタンを切り替える。
     */
    @MainThread
    void onSuccessTwitterAuth() {
        mConnectButton.setEnabled(false);
        mConnectButton.setVisibility(View.INVISIBLE);
        mSyncButton.setVisibility(View.VISIBLE);
    }

    /**
     * twitter連携に失敗した場合の処理
     */
    @MainThread
    void onFailedTwitterAuth() {
        mConnectButton.setEnabled(true);
        final LoginUtil loginUtil = new LoginUtil(this);
        loginUtil.saveLoginInfo(null);
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_error_authenticate_twitter)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    @MainThread
    void enableSyncButton() {
        // ボタンを有効化
        mSyncButton.setEnabled(true);
        refreshAll();
    }

    /**
     * 最後に表示していたステージ番号を返します。
     *
     * @return ステージ番号
     */
    private int getLastStageNo() {
        final PreferenceUtil preferenceUtil = new PreferenceUtil(
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
    private void refreshAll() {
        refreshGetStageButton();
        refreshStageCount();
        refreshSoundState();
    }

    /**
     * ステージ取得ボタンを再設定します。
     */
    private void refreshGetStageButton() {
        if (InsertDataTask.isRunning()) {
            mGetStageButton.setClickable(false);
            mGetStageButton.setText(getString(R.string.get_more_loading));
        } else {
            mGetStageButton.setClickable(true);
            mGetStageButton.setText(getString(R.string.get_more));
        }
    }

    /**
     * ステージ数領域を再設定します。
     */
    private void refreshStageCount() {
        final StageCountModel stageCountModel = kyouenDb.selectStageCount();
        stageCountView.setText(
                getString(R.string.stage_count,
                        stageCountModel.getClearStageCount(),
                        stageCountModel.getStageCount()));
    }

    /**
     * 音量領域を再設定します。
     */
    private void refreshSoundState() {
        if (SoundManager.getInstance(this).isPlayable()) {
            mSoundImageView.setImageResource(R.drawable.sound_on);
        } else {
            mSoundImageView.setImageResource(R.drawable.sound_off);
        }
    }

    private void registGcm() {
        try {
            GCMRegistrar.checkDevice(getApplicationContext());
            GCMRegistrar.checkManifest(getApplicationContext());
        } catch (final UnsupportedOperationException e) {
            Log.e("kyouen", "unsupported gcm.", e);
            return;
        }
        final String regId = GCMRegistrar
                .getRegistrationId(getApplicationContext());
        Log.i("kyouen", "regId=" + regId);
        if (regId.equals("")) {
            // GCMに登録
            GCMRegistrar.register(getApplicationContext(),
                    GCMIntentService.getSenderId(this));
            return;
        }
        if (GCMRegistrar.isRegisteredOnServer(getApplicationContext())) {
            // 既に登録されている場合、終了
            return;
        }

        final Context context = this;
        final AsyncTask<Void, Void, Void> registerTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                final boolean registered = ServerUtil.registGcm(context, regId);
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
        };
        registerTask.execute(null, null, null);
    }

    /** サーバに認証情報を送信するタスク */
    private class ServerRegistTask extends AsyncTask<AccessToken, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mConnectButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(final AccessToken... params) {
            final AccessToken token = params[0];

            // サーバに認証情報を送信
            try {
                ServerUtil.registUser(TitleActivity.this, token.getToken(),
                        token.getTokenSecret());
            } catch (final IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            mConnectButton.setEnabled(true);
            if (!result) {
                // 失敗した場合
                return;
            }
            // 成功した場合
            onSuccessTwitterAuth();
        }
    }
}
