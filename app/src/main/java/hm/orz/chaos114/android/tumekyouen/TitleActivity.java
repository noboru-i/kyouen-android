package hm.orz.chaos114.android.tumekyouen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

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
import icepick.Icepick;

/**
 * タイトル画面を表示するアクティビティ。
 */
public class TitleActivity extends AppCompatActivity {
    private static final String TAG = TitleActivity.class.getSimpleName();

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
    @Bind(R.id.adView)
    AdView mAdView;

    /** DBオブジェクト */
    private KyouenDb kyouenDb;

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
    private final DialogInterface.OnCancelListener mCancelListener = (dialog -> {
        refreshAll();
    });

    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, TitleActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        ButterKnife.bind(this);
        Icepick.restoreInstanceState(this, savedInstanceState);

        kyouenDb = new KyouenDb(this);

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 広告の表示
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final LoginUtil loginUtil = new LoginUtil(this);
        final TwitterAuthToken loginInfo = loginUtil.loadLoginInfo();
        if (loginInfo != null) {
            // 認証情報が存在する場合
            new ServerRegistTask().execute(loginInfo);
        }

        // 描画内容を更新
        refreshAll();
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        twitterAuthClient.onActivityResult(requestCode, resultCode, data);

        refreshAll();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    /**
     * スタートボタンの設定
     */
    @OnClick(R.id.start_puzzle_button)
    public void onClickStartButton() {
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
    @OnClick(R.id.connect_button)
    void onClickConnectButton() {
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
        final LoginUtil loginUtil = new LoginUtil(this);
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
        mConnectButton.setEnabled(false);
        mConnectButton.setVisibility(View.INVISIBLE);
        mSyncButton.setVisibility(View.VISIBLE);
    }

    /**
     * twitter連携に失敗した場合の処理
     */
    @MainThread
    private void onFailedTwitterAuth() {
        mConnectButton.setEnabled(true);
        final LoginUtil loginUtil = new LoginUtil(this);
        loginUtil.saveLoginInfo(null);
        new AlertDialog.Builder(this)
                .setMessage(R.string.alert_error_authenticate_twitter)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    @MainThread
    private void enableSyncButton() {
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
        final PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
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

    /** サーバに認証情報を送信するタスク */
    private class ServerRegistTask extends AsyncTask<TwitterAuthToken, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mConnectButton.setEnabled(false);
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
