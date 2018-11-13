package hm.orz.chaos114.android.tumekyouen.modules.kyouen;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.Date;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import dagger.android.support.DaggerAppCompatActivity;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog;
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityKyouenBinding;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository;
import hm.orz.chaos114.android.tumekyouen.util.AdRequestFactory;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import icepick.Icepick;
import icepick.State;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * 詰め共円のプレイ画面。
 */
public class KyouenActivity extends DaggerAppCompatActivity implements KyouenActivityHandlers {

    private static final String EXTRA_TUME_KYOUEN_MODEL
            = "hm.orz.chaos114.android.tumekyouen.EXTRA_TUME_KYOUEN_MODEL";

    @Inject
    PreferenceUtil preferenceUtil;
    @Inject
    SoundManager soundManager;
    @Inject
    TumeKyouenRepository tumeKyouenRepository;
    @Inject
    TumeKyouenService tumeKyouenService;
    @Inject
    FirebaseAnalytics firebaseAnalytics;

    // ステージ情報オブジェクト
    @State
    TumeKyouenModel stageModel;

    private ActivityKyouenBinding binding;

    // 共円描画用view
    private TumeKyouenView tumeKyouenView;

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

        Icepick.restoreInstanceState(this, savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            stageModel = (TumeKyouenModel) intent.getSerializableExtra(EXTRA_TUME_KYOUEN_MODEL);
        }
        binding.setStageModel(new KyouenActivityViewModel(stageModel, this));

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 詰め共円領域の追加
        tumeKyouenView = new TumeKyouenView(this);
        tumeKyouenView.inject(soundManager, firebaseAnalytics);
        binding.fragmentContainer.addView(tumeKyouenView);
        tumeKyouenView.setData(stageModel);

        // 広告の表示
        binding.adView.loadAd(AdRequestFactory.createAdRequest());

        // 初期化
        init();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void init() {
        // プリファレンスに設定
        preferenceUtil.putInt(PreferenceUtil.KEY_LAST_STAGE_NO, stageModel.stageNo());

        // 共円ボタンの設定
        binding.kyouenButton.setClickable(true);

        binding.kyouenOverlay.setVisibility(View.GONE);

        binding.setStageModel(new KyouenActivityViewModel(stageModel, this));
    }

    /**
     * 共円状態を設定します。
     */
    private void setKyouen() {
        binding.kyouenButton.setClickable(false);
        tumeKyouenView.setClickable(false);

        Maybe
                .concat(
                        tumeKyouenRepository.updateClearFlag(stageModel.stageNo(), new Date()).toMaybe(),
                        tumeKyouenService.add(stageModel.stageNo()),
                        tumeKyouenRepository.findStage(stageModel.stageNo())
                )
                .subscribeOn(Schedulers.io())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        obj -> {
                            Timber.d("success: %s", obj);
                            stageModel = (TumeKyouenModel) obj;
                            binding.setStageModel(new KyouenActivityViewModel(stageModel, this));
                        },
                        throwable -> Timber.d(throwable, "error"));
    }

    private void moveStage(@NonNull Direction direction) {
        Maybe<TumeKyouenModel> stageRequest = null;
        switch (direction) {
            case PREV:
                // prev選択時
                stageRequest = tumeKyouenRepository.findStage(stageModel.stageNo() - 1);
                break;
            case NEXT:
                // next選択時
                stageRequest = tumeKyouenRepository.findStage(stageModel.stageNo() + 1);
                break;
            case NONE:
                // 想定外の引数
                throw new IllegalArgumentException("引数がNONE");
        }

        stageRequest
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        newStage -> {
                            stageModel = newStage;
                            showOtherStage(direction);
                        },
                        throwable -> {
                            throw new RuntimeException("I think, we are not called this.", throwable);
                        },
                        () -> {
                            loadNextStages(direction);
                        }
                );
    }

    private void loadNextStages(@NonNull Direction direction) {
        // 次のステージが存在しない場合、APIより取得する
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        tumeKyouenRepository.selectMaxStageNo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        maxStageNo -> new InsertDataTask(this, (() -> {
                            dialog.dismiss();

                            tumeKyouenRepository.findStage(stageModel.stageNo() + 1)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                                    .subscribe(
                                            model -> {
                                                stageModel = model;
                                                showOtherStage(direction);
                                            },
                                            throwable1 -> {
                                                // no-op
                                            }
                                    );
                        }), tumeKyouenService, tumeKyouenRepository).execute(String.valueOf(maxStageNo))
                );
    }

    /**
     * stageModelのデータに合わせて画面を変更する。
     *
     * @param direction 移動するステージの方向（PREV/NEXT/NONE）
     */
    private void showOtherStage(@NonNull final Direction direction) {
        TumeKyouenView oldView = tumeKyouenView;
        tumeKyouenView = new TumeKyouenView(this);
        tumeKyouenView.inject(soundManager, firebaseAnalytics);
        tumeKyouenView.setData(stageModel);

        int width = binding.fragmentContainer.getWidth();
        float oldTranslationX = 0;
        switch (direction) {
            case PREV:
                tumeKyouenView.setTranslationX(-width);
                oldTranslationX = width;
                break;
            case NEXT:
                tumeKyouenView.setTranslationX(width);
                oldTranslationX = -width;
                break;
        }
        binding.fragmentContainer.addView(tumeKyouenView);

        oldView.animate()
                .translationX(oldTranslationX)
                .setDuration(250)
                .setInterpolator(new AccelerateInterpolator());
        tumeKyouenView.animate()
                .translationX(0)
                .setDuration(250)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.fragmentContainer.removeView(oldView);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        init();
    }

    @Override
    public void onClickCheckKyouen(View view) {
        if (tumeKyouenView.getGameModel().getWhiteStoneCount() != 4) {
            // 4つの石が選択されていない場合
            new AlertDialog.Builder(KyouenActivity.this)
                    .setTitle(R.string.alert_less_stone)
                    .setPositiveButton("OK", null).create().show();
            return;
        }
        final KyouenData data = tumeKyouenView.getGameModel().isKyouen();
        if (data == null) {
            // 共円でない場合
            new AlertDialog.Builder(KyouenActivity.this)
                    .setTitle(R.string.alert_not_kyouen)
                    .setPositiveButton("OK", null).create().show();
            // 全ての石を未選択状態に戻す
            tumeKyouenView.reset();
            return;
        }

        // 共円の場合
        soundManager.play(R.raw.se_maoudamashii_onepoint23);
        new AlertDialog.Builder(KyouenActivity.this)
                .setTitle(R.string.kyouen)
                .setNeutralButton("Next", ((dialog, which) -> moveStage(Direction.NEXT)))
                .create().show();
        binding.kyouenOverlay.setData(stageModel.size(), data);
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
                KyouenActivity.this,
                ((count) ->
                        tumeKyouenRepository.selectMaxStageNo()
                                .subscribeOn(Schedulers.io())
                                .flatMapMaybe(maxStageNo -> {
                                    int nextStageNo = count;
                                    if (nextStageNo > maxStageNo || nextStageNo == -1) {
                                        nextStageNo = maxStageNo;
                                    }
                                    return tumeKyouenRepository.findStage(nextStageNo);
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                                .subscribe(
                                        model -> {
                                            Direction direction;
                                            if (stageModel.stageNo() > model.stageNo()) {
                                                direction = Direction.PREV;
                                            } else if (stageModel.stageNo() < model.stageNo()) {
                                                direction = Direction.NEXT;
                                            } else {
                                                return;
                                            }
                                            stageModel = model;
                                            showOtherStage(direction);
                                        },
                                        throwable -> {
                                            // no-op
                                        }
                                )), null);
        dialog.setStageNo(stageModel.stageNo());
        dialog.show();
    }

    /**
     * 方向を表すenum
     */
    private enum Direction {
        PREV, NEXT, NONE
    }
}