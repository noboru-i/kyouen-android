package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog;
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityKyouenBinding;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import rx.schedulers.Schedulers;

/**
 * 詰め共円のプレイ画面。
 */
public class KyouenActivity extends AppCompatActivity implements KyouenActivityHandlers {

    private static final String EXTRA_TUME_KYOUEN_MODEL
            = "hm.orz.chaos114.android.tumekyouen.EXTRA_TUME_KYOUEN_MODEL";

    @Inject
    PreferenceUtil preferenceUtil;
    @Inject
    SoundManager soundManager;
    @Inject
    KyouenDb kyouenDb;
    @Inject
    TumeKyouenService tumeKyouenService;

    /** ステージ情報オブジェクト */
    private TumeKyouenModel stageModel;

    private ActivityKyouenBinding binding;

    /** 共円描画用view */
    private TumeKyouenFragment tumeKyouenFragment;

    public static void start(Activity activity, TumeKyouenModel tumeKyouenModel) {
        final Intent intent = new Intent(activity, KyouenActivity.class);
        intent.putExtra(EXTRA_TUME_KYOUEN_MODEL, tumeKyouenModel);
        activity.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kyouen);
        binding.setHandlers(this);

        getApplicationComponent().inject(this);

        Intent intent = getIntent();
        if (intent != null) {
            stageModel = (TumeKyouenModel) intent.getSerializableExtra(EXTRA_TUME_KYOUEN_MODEL);
        }
        binding.setStageModel(new KyouenActivityViewModel(stageModel, this));

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (savedInstanceState == null) {
            // 詰め共円領域の追加
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            tumeKyouenFragment = TumeKyouenFragment.newInstance(stageModel);
            fragmentTransaction.add(R.id.fragment_container, tumeKyouenFragment);
            fragmentTransaction.commit();
        }

        // 広告の表示
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        // 初期化
        init();
    }

    private AppComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }

    private void init() {
        // プリファレンスに設定
        preferenceUtil.putInt(PreferenceUtil.KEY_LAST_STAGE_NO, stageModel.getStageNo());

        // 共円ボタンの設定
        binding.kyouenButton.setClickable(true);

        binding.kyouenOverlay.setVisibility(View.INVISIBLE);

        binding.setStageModel(new KyouenActivityViewModel(stageModel, this));
    }

    /**
     * 共円状態を設定します。
     */
    private void setKyouen() {
        binding.kyouenButton.setClickable(false);
        tumeKyouenFragment.setClickable(false);

        stageModel.setClearFlag(TumeKyouenModel.CLEAR);
        kyouenDb.updateClearFlag(stageModel);

        // サーバに送信
        tumeKyouenService.add(stageModel.getStageNo())
                .subscribeOn(Schedulers.io())
                .subscribe();

        binding.setStageModel(new KyouenActivityViewModel(stageModel, this));
    }

    /**
     * ステージの移動処理を行います。
     *
     * @param direction 移動するステージの方向（PREV/NEXT）
     * @return 移動が成功した場合true
     */
    private boolean moveStage(@NonNull final Direction direction) {
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

            final long maxStageNo = kyouenDb.selectMaxStageNo();
            new InsertDataTask(this, (() -> {
                dialog.dismiss();

                final TumeKyouenModel model = kyouenDb.selectNextStage(stageModel.getStageNo());
                if (model == null) {
                    // WEBより取得後も取得できない場合
                    return;
                }

                stageModel = model;
                showOtherStage(direction);
            }))
                    .execute(String.valueOf(maxStageNo));

            return false;
        }

        stageModel = newModel;
        showOtherStage(direction);
        return true;
    }

    /**
     * stageModelのデータに合わせて画面を変更する。
     *
     * @param direction 移動するステージの方向（PREV/NEXT/NONE）
     */
    private void showOtherStage(@NonNull final Direction direction) {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        tumeKyouenFragment = TumeKyouenFragment.newInstance(stageModel);

        switch (direction) {
            case PREV:
                ft.setCustomAnimations(
                        R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_right_exit);
                break;
            case NEXT:
                ft.setCustomAnimations(
                        R.anim.fragment_slide_left_enter,
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

    @Override
    public void onClickCheckKyouen(View view) {
        if (tumeKyouenFragment.getGameModel().getWhiteStoneCount() != 4) {
            // 4つの石が選択されていない場合
            new AlertDialog.Builder(KyouenActivity.this)
                    .setTitle(R.string.alert_less_stone)
                    .setPositiveButton("OK", null).create().show();
            return;
        }
        final KyouenData data = tumeKyouenFragment.getGameModel().isKyouen();
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
        soundManager.play(R.raw.se_maoudamashii_onepoint23);
        new AlertDialog.Builder(KyouenActivity.this)
                .setTitle(R.string.kyouen)
                .setNeutralButton("Next", ((dialog, which) -> moveStage(Direction.NEXT)))
                .create().show();
        binding.kyouenOverlay.setData(stageModel.getSize(), data);
        binding.kyouenOverlay.setVisibility(View.VISIBLE);
        setKyouen();
    }

    @Override
    public void onClickMoveStage(View v) {

        Direction direction;
        if (v == binding.prevButton) {
            // prevボタン押下時
            direction = Direction.PREV;
        } else {
            // nextボタン押下時
            direction = Direction.NEXT;
        }

        moveStage(direction);
    }

    @Override
    public void showSelectStageDialog(View view) {
        final StageSelectDialog dialog = new StageSelectDialog(
                KyouenActivity.this, ((count) -> {
            final long maxStageNo = kyouenDb.selectMaxStageNo();
            int nextStageNo = count;
            if (nextStageNo > maxStageNo || nextStageNo == -1) {
                nextStageNo = (int) maxStageNo;
            }

            // ダイアログで選択されたステージを表示
            stageModel = kyouenDb.selectCurrentStage(nextStageNo);
            showOtherStage(Direction.NONE);
        }), null);
        dialog.setStageNo(stageModel.getStageNo());
        dialog.show();
    }

    /** 方向を表すenum */
    private enum Direction {
        PREV, NEXT, NONE
    }
}