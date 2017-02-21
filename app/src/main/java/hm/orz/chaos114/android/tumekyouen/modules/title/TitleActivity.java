package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

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
import hm.orz.chaos114.android.tumekyouen.network.NewKyouenService;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import hm.orz.chaos114.android.tumekyouen.network.entity.AuthInfo;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PackageChecker;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * タイトル画面を表示するアクティビティ。
 */
public class TitleActivity extends AppCompatActivity implements TitleActivityHandlers {
    @Inject
    LoginUtil loginUtil;
    @Inject
    PreferenceUtil preferenceUtil;
    @Inject
    SoundManager soundManager;
    @Inject
    KyouenDb kyouenDb;
    @Inject
    TumeKyouenService tumeKyouenService;
    @Inject
    NewKyouenService kyouenService;

    private ActivityTitleBinding binding;

    private TwitterAuthClient twitterAuthClient = new TwitterAuthClient();

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
        Timber.d("loginInfo = %s", loginInfo);
        if (loginInfo != null) {
            // 認証情報が存在する場合
            binding.connectButton.setEnabled(false);
            kyouenService.login(new AuthInfo(loginInfo.token, loginInfo.secret))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                                Timber.d("sucess : %s", s);
                                binding.connectButton.setEnabled(true);
                                // 成功した場合
                                onSuccessTwitterAuth();
                            },
                            throwable -> {
                                Timber.d(throwable, "fail");
                                binding.connectButton.setEnabled(true);
                            });
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
                (count -> {
                    int taskCount = count == -1 ? Integer.MAX_VALUE : count;
                    final InsertDataTask task = new InsertDataTask(TitleActivity.this,
                            taskCount, this::refreshAll, kyouenService);
                    final long maxStageNo = kyouenDb.selectMaxStageNo();
                    task.execute(String.valueOf(maxStageNo));

                }),
                (d -> refreshAll()));
        dialog.show();
    }

    /**
     * ステージ作成ボタン押下時の処理
     */
    @Override
    public void onClickCreateStage(View v) {
        final String packageName = "hm.orz.chaos114.android.kyouenchecker";
        if (PackageChecker.check(this, packageName)) {
            // 共円チェッカーの起動
            final Intent intent = new Intent();
            intent.setClassName(packageName, packageName + ".KyouenActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // マーケットへの導線を表示
            new AlertDialog.Builder(this)
                    .setMessage(R.string.alert_install_kyouenchecker)
                    .setPositiveButton("YES", ((dialog, which) -> {
                        // マーケットを開く
                        final Uri uri = Uri.parse("market://details?id=" + packageName);
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
                Timber.d("success");
                dialog.dismiss();
                sendAuthToken(result.data.getAuthToken());
            }

            @Override
            public void failure(TwitterException e) {
                Timber.d("failure");
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
        // サーバに認証情報を送信
        kyouenService.login(new AuthInfo(authToken.token, authToken.secret))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            // ログイン情報を保存
                            loginUtil.saveLoginInfo(authToken);
                            onSuccessTwitterAuth();
                        },
                        throwable -> {
                            onFailedTwitterAuth();
                        });
    }

    /**
     * クリアステージデータの同期を行う。
     */
    @WorkerThread
    private void syncClearDataInBackground() {
        // クリアした情報を取得
        final List<TumeKyouenModel> stages = kyouenDb.selectAllClearStage();

        ServerUtil.addAll(tumeKyouenService, stages)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            if (s.getData() != null) {
                                kyouenDb.updateSyncClearData(s.getData());
                            }
                            refresh();
                        },
                        throwable -> {
                            Timber.e(throwable, "クリア情報の送信に失敗");
                        });
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
}
