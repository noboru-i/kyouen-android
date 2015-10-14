package hm.orz.chaos114.android.tumekyouen;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;

import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.fragment.TumeKyouenFragment;
import hm.orz.chaos114.android.tumekyouen.model.KyouenData;
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel;
import hm.orz.chaos114.android.tumekyouen.util.InsertDataTask;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

@NoTitle
@EActivity(R.layout.main)
public class KyouenActivity extends FragmentActivity {
    /** ステージ情報オブジェクト */
    @Extra("item")
    TumeKyouenModel stageModel;
    /** 共円描画用View */
    @ViewById(R.id.kyouen_overlay)
    OverlayView overlayView;
    @ViewById(R.id.prev_button)
    Button prevButton;
    @ViewById(R.id.next_button)
    Button nextButton;
    @ViewById(R.id.stage_no_layout)
    LinearLayout stageNoLayout;
    @ViewById(R.id.stage_no)
    TextView stageNoView;
    /** DBアクセスオブジェクト */
    private KyouenDb kyouenDb;
    /** 共円描画用view */
    private TumeKyouenFragment tumeKyouenFragment;

    @AfterViews
    void afterViews() {
        kyouenDb = new KyouenDb(this);

        // 音量ボタンの動作変更
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 詰め共円領域の追加
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        tumeKyouenFragment = TumeKyouenFragment.newInstance(stageModel);
        fragmentTransaction.add(R.id.fragment_container, tumeKyouenFragment);
        fragmentTransaction.commit();

        // 広告の表示
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // 初期化
        init();
    }

    private void init() {
        // プリファレンスに設定
        final PreferenceUtil preferenceUtil = new PreferenceUtil(
                getApplicationContext());
        preferenceUtil.putInt(PreferenceUtil.KEY_LAST_STAGE_NO, stageModel.getStageNo());

        // ステージ名表示領域の設定
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
        final TextView stageCreatorView = (TextView) findViewById(R.id.stage_creator);
        stageCreatorView.setText("created by " + stageModel.getCreator());

        // prev,nextボタンの設定
        final Button prevButton = (Button) findViewById(R.id.prev_button);
        if (stageModel.getStageNo() == 1) {
            // 先頭ステージの場合は押下不可
            prevButton.setClickable(false);
        } else {
            prevButton.setClickable(true);
        }

        // 共円ボタンの設定
        final Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
        kyouenButton.setClickable(true);

        overlayView.setVisibility(View.INVISIBLE);
    }

    /**
     * 共円状態を設定します。
     */
    private void setKyouen() {
        final Button kyouenButton = (Button) findViewById(R.id.kyouen_button);
        kyouenButton.setClickable(false);
        tumeKyouenFragment.setClickable(false);

        stageModel.setClearFlag(TumeKyouenModel.CLEAR);
        kyouenDb.updateClearFlag(stageModel);

        // サーバに送信
        final AddStageUserTask task = new AddStageUserTask();
        task.execute(stageModel);

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

            final InsertDataTask task = new InsertDataTask(this,
                    new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();

                            final TumeKyouenModel newModel = kyouenDb
                                    .selectNextStage(stageModel.getStageNo());
                            if (newModel == null) {
                                // WEBより取得後も取得できない場合
                                return;
                            }

                            stageModel = newModel;
                            showOtherStage(direction);
                        }
                    });
            final long maxStageNo = kyouenDb.selectMaxStageNo();
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
    private void showOtherStage(final Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("引数がnull");
        }
        final FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
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

    @Click(R.id.kyouen_button)
    void checkKyouen() {
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
        SoundManager.getInstance(KyouenActivity.this).play(
                R.raw.se_maoudamashii_onepoint23);
        new AlertDialog.Builder(KyouenActivity.this)
                .setTitle(R.string.kyouen)
                .setNeutralButton("Next",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                moveStage(Direction.NEXT);
                            }
                        }).create().show();
        overlayView.setData(stageModel.getSize(), data);
        overlayView.setVisibility(View.VISIBLE);
        setKyouen();
    }

    @Click({R.id.next_button, R.id.prev_button})
    void moveStage(final View v) {

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

    @Click({R.id.stage_no_layout})
    void showSelectStageDialog() {
        final StageSelectDialog dialog = new StageSelectDialog(
                KyouenActivity.this, new StageSelectDialog.OnSuccessListener() {
            @Override
            public void onSuccess(final int count) {
                final long maxStageNo = kyouenDb.selectMaxStageNo();
                int nextStageNo = count;
                if (nextStageNo > maxStageNo || nextStageNo == -1) {
                    nextStageNo = (int) maxStageNo;
                }

                // ダイアログで選択されたステージを表示
                final TumeKyouenModel newModel = kyouenDb
                        .selectCurrentStage(nextStageNo);
                stageModel = newModel;
                showOtherStage(Direction.NONE);
            }
        }, null);
        dialog.setStageNo(stageModel.getStageNo());
        dialog.show();
    }

    /** 方向を表すenum */
    enum Direction {
        PREV, NEXT, NONE
    }

    /**
     * クリア情報を送信するタスククラス
     *
     * @author ishikuranoboru
     */
    final class AddStageUserTask extends AsyncTask<TumeKyouenModel, Void, Void> {
        @Override
        protected Void doInBackground(final TumeKyouenModel... params) {
            ServerUtil.addStageUser(KyouenActivity.this, params[0]);
            return null;
        }
    }

    ;
}