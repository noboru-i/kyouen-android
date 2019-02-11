package hm.orz.chaos114.android.tumekyouen.modules.kyouen

import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.app.StageSelectDialog
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityKyouenBinding
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.AdRequestFactory
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import icepick.Icepick
import icepick.State
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * 詰め共円のプレイ画面。
 */
class KyouenActivity : DaggerAppCompatActivity(), KyouenActivityHandlers {

    @Inject
    internal lateinit var preferenceUtil: PreferenceUtil
    @Inject
    internal lateinit var soundManager: SoundManager
    @Inject
    internal lateinit var tumeKyouenRepository: TumeKyouenRepository
    @Inject
    internal lateinit var tumeKyouenService: TumeKyouenService
    @Inject
    internal lateinit var firebaseAnalytics: FirebaseAnalytics
    @Inject
    internal lateinit var insertDataTask: InsertDataTask

    // ステージ情報オブジェクト
    @State
    @JvmField
    internal var stageModel: TumeKyouenModel? = null

    private lateinit var binding: ActivityKyouenBinding

    // 共円描画用view
    private lateinit var tumeKyouenView: TumeKyouenView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kyouen)
        binding.handlers = this

        Icepick.restoreInstanceState(this, savedInstanceState)

        val intent = intent
        if (intent != null) {
            stageModel = intent.getSerializableExtra(EXTRA_TUME_KYOUEN_MODEL) as TumeKyouenModel
        }
        binding.stageModel = KyouenActivityViewModel(stageModel!!, this)

        // 音量ボタンの動作変更
        volumeControlStream = AudioManager.STREAM_MUSIC

        // 詰め共円領域の追加
        tumeKyouenView = TumeKyouenView(this)
        tumeKyouenView.inject(soundManager, firebaseAnalytics)
        binding.fragmentContainer.addView(tumeKyouenView)
        tumeKyouenView.setData(stageModel!!)

        // 広告の表示
        binding.adView.loadAd(AdRequestFactory.createAdRequest())

        // 初期化
        init()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    private fun init() {
        // プリファレンスに設定
        preferenceUtil.putInt(PreferenceUtil.KEY_LAST_STAGE_NO, stageModel!!.stageNo)

        // 共円ボタンの設定
        binding.kyouenButton.isClickable = true

        binding.kyouenOverlay.visibility = View.GONE

        binding.stageModel = KyouenActivityViewModel(stageModel!!, this)
    }

    /**
     * 共円状態を設定します。
     */
    private fun setKyouen() {
        binding.kyouenButton.isClickable = false
        tumeKyouenView.isClickable = false

        Maybe
                .concat(
                        tumeKyouenRepository.updateClearFlag(stageModel!!.stageNo, Date()).toMaybe(),
                        tumeKyouenService.add(stageModel!!.stageNo),
                        tumeKyouenRepository.findStage(stageModel!!.stageNo)
                )
                .subscribeOn(Schedulers.io())
                .`as`<FlowableSubscribeProxy<Any>>(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { obj ->
                            Timber.d("success: %s", obj)
                            stageModel = obj as TumeKyouenModel
                            binding.stageModel = KyouenActivityViewModel(stageModel!!, this)
                        },
                        { throwable -> Timber.d(throwable, "error") })
    }

    private fun moveStage(direction: Direction) {
        var stageRequest: Maybe<TumeKyouenModel>? = null
        when (direction) {
            KyouenActivity.Direction.PREV ->
                // prev選択時
                stageRequest = tumeKyouenRepository.findStage(stageModel!!.stageNo - 1)
            KyouenActivity.Direction.NEXT ->
                // next選択時
                stageRequest = tumeKyouenRepository.findStage(stageModel!!.stageNo + 1)
            KyouenActivity.Direction.NONE ->
                // 想定外の引数
                throw IllegalArgumentException("引数がNONE")
        }

        stageRequest
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<MaybeSubscribeProxy<TumeKyouenModel>>(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { newStage ->
                            stageModel = newStage
                            showOtherStage(direction)
                        },
                        { throwable -> throw RuntimeException("I think, we are not called this.", throwable) },
                        { loadNextStages(direction) }
                )
    }

    private fun loadNextStages(direction: Direction) {
        // 次のステージが存在しない場合、APIより取得する
        val dialog = ProgressDialog(this)
        dialog.setMessage("Loading...")
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.show()

        tumeKyouenRepository.selectMaxStageNo()
                .subscribeOn(Schedulers.io())
                .flatMap { maxStageNo -> insertDataTask.run(maxStageNo, 1) }
                .flatMapMaybe { count -> tumeKyouenRepository.findStage(stageModel!!.stageNo + 1) }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<MaybeSubscribeProxy<TumeKyouenModel>>(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { model ->
                            dialog.dismiss()
                            stageModel = model
                            showOtherStage(direction)
                        },
                        { throwable1 ->
                            // no-op
                        },
                        {
                            Timber.d("loadNextStages onComplete")
                            dialog.dismiss()
                        }
                )
    }

    /**
     * stageModelのデータに合わせて画面を変更する。
     *
     * @param direction 移動するステージの方向（PREV/NEXT/NONE）
     */
    private fun showOtherStage(direction: Direction) {
        val oldView = tumeKyouenView
        tumeKyouenView = TumeKyouenView(this)
        tumeKyouenView.inject(soundManager, firebaseAnalytics)
        tumeKyouenView.setData(stageModel!!)

        val width = binding.fragmentContainer.width
        var oldTranslationX = 0f
        when (direction) {
            KyouenActivity.Direction.PREV -> {
                tumeKyouenView.translationX = (-width).toFloat()
                oldTranslationX = width.toFloat()
            }
            KyouenActivity.Direction.NEXT -> {
                tumeKyouenView.translationX = width.toFloat()
                oldTranslationX = (-width).toFloat()
            }
            Direction.NONE -> {
                // no-op
            }
        }
        binding.fragmentContainer.addView(tumeKyouenView)

        oldView.animate()
                .translationX(oldTranslationX)
                .setDuration(250).interpolator = AccelerateInterpolator()
        tumeKyouenView.animate()
                .translationX(0f)
                .setDuration(250)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        binding.fragmentContainer.removeView(oldView)
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })

        init()
    }

    override fun onClickCheckKyouen(view: View) {
        if (tumeKyouenView.gameModel.whiteStoneCount != 4) {
            // 4つの石が選択されていない場合
            AlertDialog.Builder(this@KyouenActivity)
                    .setTitle(R.string.alert_less_stone)
                    .setPositiveButton("OK", null).create().show()
            return
        }
        val data = tumeKyouenView.gameModel.isKyouen
        if (data == null) {
            // 共円でない場合
            AlertDialog.Builder(this@KyouenActivity)
                    .setTitle(R.string.alert_not_kyouen)
                    .setPositiveButton("OK", null).create().show()
            // 全ての石を未選択状態に戻す
            tumeKyouenView.reset()
            return
        }

        // 共円の場合
        soundManager.play(R.raw.se_maoudamashii_onepoint23)
        AlertDialog.Builder(this@KyouenActivity)
                .setTitle(R.string.kyouen)
                .setNeutralButton("Next") { dialog, which -> moveStage(Direction.NEXT) }
                .create().show()
        binding.kyouenOverlay.setData(stageModel!!.size, data)
        binding.kyouenOverlay.visibility = View.VISIBLE
        setKyouen()
    }

    override fun onClickMoveStage(v: View) {

        val direction: Direction
        if (v === binding.prevButton) {
            // prevボタン押下時
            direction = Direction.PREV
        } else {
            // nextボタン押下時
            direction = Direction.NEXT
        }

        moveStage(direction)
    }

    override fun showSelectStageDialog(view: View) {
        val dialog = StageSelectDialog(
                this@KyouenActivity,
                object : StageSelectDialog.OnSuccessListener {
                    override fun onSuccess(count: Int) {
                        tumeKyouenRepository.selectMaxStageNo()
                                .subscribeOn(Schedulers.io())
                                .flatMapMaybe { maxStageNo ->
                                    var nextStageNo = count
                                    if (nextStageNo > maxStageNo || nextStageNo == -1) {
                                        nextStageNo = maxStageNo
                                    }
                                    tumeKyouenRepository.findStage(nextStageNo)
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .`as`<MaybeSubscribeProxy<TumeKyouenModel>>(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@KyouenActivity)))
                                .subscribe(
                                        { model ->
                                            val direction: Direction
                                            if (stageModel!!.stageNo > model.stageNo) {
                                                direction = Direction.PREV
                                            } else if (stageModel!!.stageNo < model.stageNo) {
                                                direction = Direction.NEXT
                                            } else {
                                                return@subscribe
                                            }
                                            stageModel = model
                                            showOtherStage(direction)
                                        },
                                        { throwable ->
                                            // no-op
                                        }
                                )
                    }
                }, null)
        dialog.setStageNo(stageModel!!.stageNo)
        dialog.show()
    }

    /**
     * 方向を表すenum
     */
    private enum class Direction {
        PREV, NEXT, NONE
    }

    companion object {

        private val EXTRA_TUME_KYOUEN_MODEL = "hm.orz.chaos114.android.tumekyouen.EXTRA_TUME_KYOUEN_MODEL"

        fun start(activity: Activity, tumeKyouenModel: TumeKyouenModel) {
            val intent = Intent(activity, KyouenActivity::class.java)
            intent.putExtra(EXTRA_TUME_KYOUEN_MODEL, tumeKyouenModel)
            activity.startActivityForResult(intent, 0)
        }
    }
}