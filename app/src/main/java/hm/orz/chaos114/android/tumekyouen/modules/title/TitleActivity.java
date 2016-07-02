package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog;
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityTitleBinding;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

/**
 * タイトル画面を表示するアクティビティ。
 */
public class TitleActivity extends AppCompatActivity implements TitleActivityHandlers {
    private static final String TAG = TitleActivity.class.getSimpleName();

    @Inject
    LoginUtil loginUtil;
    @Inject
    PreferenceUtil preferenceUtil;
    @Inject
    SoundManager soundManager;
    @Inject
    KyouenDb kyouenDb;

    private ActivityTitleBinding binding;

    private TwitterAuthClient twitterAuthClient = new TwitterAuthClient();

    /** 取得ボタン押下後の処理 */
    private final StageGetDialog.OnSuccessListener mSuccessListener = (count -> {
        int taskCount = count == -1 ? Integer.MAX_VALUE : count;
        final InsertDataTask task = new InsertDataTask(TitleActivity.this,
                taskCount, this::refreshAll);
        final long maxStageNo = kyouenDb.selectMaxStageNo();
        task.execute(String.valueOf(maxStageNo));

    });

    /** キャンセルボタン押下後の処理 */
    private final DialogInterface.OnCancelListener mCancelListener = (dialog -> refreshAll());

    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, TitleActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_title);
        binding.setHandlers(this);

        getApplicationComponent().inject(this);

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 広告の表示
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        final TwitterAuthToken loginInfo = loginUtil.loadLoginInfo();
        if (loginInfo != null) {
            // 認証情報が存在する場合
            new ServerRegistTask().execute(loginInfo);
        }

        // 描画内容を更新
        refreshAll();
    }

    private AppComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        twitterAuthClient.onActivityResult(requestCode, resultCode, data);

        refreshAll();
    }

    /**
     * スタートボタンの設定
     */
    @Override
    public void onClickStartButton(View view) {
        final int stageNo = getLastStageNo();
        final TumeKyouenModel item = kyouenDb.selectCurrentStage(stageNo);
        KyouenActivity.start(this, item);
    }

    /**
     * ステージ取得ボタンの設定
     */
    @Override
    public void onClickGetStage(View v) {
        v.setClickable(false);
        ((Button) v).setText(getString(R.string.get_more_loading));

        final StageGetDialog dialog = new StageGetDialog(this,
                mSuccessListener, mCancelListener);
        dialog.show();
    }

    /**
     * ステージ作成ボタン押下時の処理
     */
    @Override
    public void onClickCreateStage(View v) {
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
                    .setPositiveButton("YES", ((dialog, which) -> {
                        // マーケットを開く
                        final Uri uri = Uri.parse("market://details?id=hm.orz.chaos114.android.kyouenchecker");
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }))
                    .setNegativeButton("NO", null).show();
        }
    }

    /** twitter接続ボタン押下後の処理 */
    @Override
    public void onClickConnectButton(View view) {
        ProgressDialog dialog;
        // ローディングダイアログの表示
        dialog = new ProgressDialog(TitleActivity.this);
        dialog.setMessage("Now Loading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        twitterAuthClient.authorize(TitleActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "success");
                dialog.dismiss();
                sendAuthToken(result.data.getAuthToken());
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "failure");
                new AlertDialog.Builder(TitleActivity.this)
                        .setMessage(R.string.alert_error_authenticate_twitter)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                dialog.dismiss();
            }
        });
    }

    /**
     * クリア情報を同期ボタン押下時の処理
     */
    @Override
    public void onClickSyncButton(View view) {
        // ボタンを無効化
        binding.syncButton.setEnabled(false);

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
    @Override
    public void switchPlayable(View view) {
        soundManager.switchPlayable();
        refresh();
    }

    @MainThread
    private void sendAuthToken(TwitterAuthToken authToken) {
        new AsyncTask<TwitterAuthToken, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(TwitterAuthToken... authToken) {
                return authTwitterInBackground(authToken[0]);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    onSuccessTwitterAuth();
                } else {
                    onFailedTwitterAuth();
                }
            }
        }.execute(authToken);
    }

    /**
     * クリアステージデータの同期を行う。
     */
    @WorkerThread
    private void syncClearDataInBackground() {
        // クリアした情報を取得
        final List<TumeKyouenModel> stages = kyouenDb.selectAllClearStage();
        // ステージデータを送信
        final List<TumeKyouenModel> clearList = ServerUtil.addAllStageUser(this, stages);
        if (clearList != null) {
            kyouenDb.updateSyncClearData(clearList);
        }
    }

    @WorkerThread
    private boolean authTwitterInBackground(final TwitterAuthToken authToken) {
        // サーバに認証情報を送信
        try {
            Log.d(TAG, "ServerUtil.registUser");
            ServerUtil.registUser(this, authToken.token, authToken.secret);
        } catch (final IOException e) {
            return false;
        }

        // ログイン情報を保存
        Log.d(TAG, "loginUtil.saveLoginInfo");
        loginUtil.saveLoginInfo(authToken);

        return true;
    }

    /**
     * twitter連携に成功した場合の処理。
     * ボタンを切り替える。
     */
    @MainThread
    private void onSuccessTwitterAuth() {
        binding.connectButton.setEnabled(false);
        binding.connectButton.setVisibility(View.INVISIBLE);
        binding.syncButton.setVisibility(View.VISIBLE);
    }

    /**
     * twitter連携に失敗した場合の処理
     */
    @MainThread
    private void onFailedTwitterAuth() {
        binding.connectButton.setEnabled(true);
        loginUtil.saveLoginInfo(null);
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_error_authenticate_twitter)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    @MainThread
    private void enableSyncButton() {
        // ボタンを有効化
        binding.syncButton.setEnabled(true);
        refreshAll();
    }

    /**
     * 最後に表示していたステージ番号を返します。
     *
     * @return ステージ番号
     */
    private int getLastStageNo() {
        int lastStageNo = preferenceUtil.getInt(PreferenceUtil.KEY_LAST_STAGE_NO);
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
        refresh();
    }

    /**
     * ステージ取得ボタンを再設定します。
     */
    private void refreshGetStageButton() {
        if (InsertDataTask.isRunning()) {
            binding.getStageButton.setClickable(false);
            binding.getStageButton.setText(getString(R.string.get_more_loading));
        } else {
            binding.getStageButton.setClickable(true);
            binding.getStageButton.setText(getString(R.string.get_more));
        }
    }

    /**
     * ステージ数領域を再設定します。
     */
    private void refresh() {
        final StageCountModel stageCountModel = kyouenDb.selectStageCount();
        App app = (App) getApplication();
        binding.setModel(new TitleActivityViewModel(app, stageCountModel, this));
    }

    /** サーバに認証情報を送信するタスク */
    private class ServerRegistTask extends AsyncTask<TwitterAuthToken, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            binding.connectButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(final TwitterAuthToken... params) {
            final TwitterAuthToken token = params[0];

            // サーバに認証情報を送信
            try {
                ServerUtil.registUser(TitleActivity.this, token.token, token.secret);
            } catch (final IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            binding.connectButton.setEnabled(true);
            if (!result) {
                // 失敗した場合
                return;
            }
            // 成功した場合
            onSuccessTwitterAuth();
        }
    }
}
