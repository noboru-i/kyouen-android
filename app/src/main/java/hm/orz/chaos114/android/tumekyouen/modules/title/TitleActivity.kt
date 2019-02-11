package hm.orz.chaos114.android.tumekyouen.modules.title

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

import javax.inject.Inject

import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityTitleBinding
import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse
import hm.orz.chaos114.android.tumekyouen.model.LoginResult
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.modules.create.CreateActivity
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.AdRequestFactory
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * タイトル画面を表示するアクティビティ。
 */
class TitleActivity : DaggerAppCompatActivity(), TitleActivityHandlers {
    @Inject
    internal lateinit var loginUtil: LoginUtil
    @Inject
    internal lateinit var preferenceUtil: PreferenceUtil
    @Inject
    internal lateinit var soundManager: SoundManager
    @Inject
    internal lateinit var tumeKyouenRepository: TumeKyouenRepository
    @Inject
    internal lateinit var tumeKyouenService: TumeKyouenService
    @Inject
    internal lateinit var insertDataTask: InsertDataTask

    private lateinit var binding: ActivityTitleBinding

    private val twitterAuthClient = TwitterAuthClient()

    /**
     * 最後に表示していたステージ番号を返します。
     *
     * @return ステージ番号
     */
    private// デフォルト値を設定
    val lastStageNo: Int
        get() {
            var lastStageNo = preferenceUtil.getInt(PreferenceUtil.KEY_LAST_STAGE_NO)
            if (lastStageNo == 0) {
                lastStageNo = 1
            }

            return lastStageNo
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_title)
        binding.handlers = this

        // 音量ボタンの動作変更
        volumeControlStream = AudioManager.STREAM_MUSIC

        // 広告の表示
        binding.adView.loadAd(AdRequestFactory.createAdRequest())

        val loginInfo = loginUtil.loadLoginInfo()
        Timber.d("loginInfo = %s", loginInfo)
        if (loginInfo != null) {
            // 認証情報が存在する場合
            binding.connectButton.isEnabled = false
            tumeKyouenService.login(loginInfo.token, loginInfo.secret)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .`as`<SingleSubscribeProxy<LoginResult>>(AutoDispose.autoDisposable<LoginResult>(AndroidLifecycleScopeProvider.from(this)))
                    .subscribe({ s ->
                        Timber.d("sucess : %s", s)
                        binding.connectButton.isEnabled = true
                        // 成功した場合
                        onSuccessTwitterAuth()
                    },
                            { throwable ->
                                Timber.d(throwable, "fail")
                                binding.connectButton.isEnabled = true
                            })
        }

        // 描画内容を更新
        refreshAll()
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        twitterAuthClient.onActivityResult(requestCode, resultCode, data)

        refreshAll()
    }

    /**
     * スタートボタンの設定
     */
    override fun onClickStartButton(view: View) {
        val stageNo = lastStageNo
        tumeKyouenRepository.findStage(stageNo)
                .subscribeOn(Schedulers.io())
                .`as`<MaybeSubscribeProxy<TumeKyouenModel>>(AutoDispose.autoDisposable<TumeKyouenModel>(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { item -> KyouenActivity.start(this, item) }
                )
    }

    /**
     * ステージ取得ボタンの設定
     */
    override fun onClickGetStage(v: View) {
        v.isClickable = false
        (v as Button).text = getString(R.string.get_more_loading)

        val dialog = StageGetDialog(this,
                object : StageGetDialog.OnSuccessListener {
                    override fun onSuccess(count: Int) {
                        val taskCount = if (count == -1) Integer.MAX_VALUE else count
                        tumeKyouenRepository.selectMaxStageNo()
                                .subscribeOn(Schedulers.io())
                                .flatMap { maxStageNo -> insertDataTask.run(maxStageNo, taskCount) }
                                .observeOn(AndroidSchedulers.mainThread())
                                .`as`<SingleSubscribeProxy<Int>>(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this@TitleActivity)))
                                .subscribe(
                                        { successCount ->
                                            Toast.makeText(this@TitleActivity,
                                                    getString(R.string.toast_get_stage, successCount),
                                                    Toast.LENGTH_SHORT).show()
                                            refresh()
                                        },
                                        { throwable ->
                                            Toast.makeText(this@TitleActivity,
                                                    R.string.toast_no_stage,
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                )
                    }
                },
                DialogInterface.OnCancelListener { d -> refreshAll() })
        dialog.show()
    }

    /**
     * ステージ作成ボタン押下時の処理
     */
    override fun onClickCreateStage(v: View) {
        CreateActivity.start(this)
    }

    /**
     * twitter接続ボタン押下後の処理
     */
    override fun onClickConnectButton(view: View) {
        val dialog: ProgressDialog
        // ローディングダイアログの表示
        dialog = ProgressDialog(this@TitleActivity)
        dialog.setMessage("Now Loading...")
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.show()

        twitterAuthClient.authorize(this@TitleActivity, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Timber.d("success")
                dialog.dismiss()
                sendAuthToken(result.data.authToken)
            }

            override fun failure(e: TwitterException) {
                Timber.d("failure")
                AlertDialog.Builder(this@TitleActivity)
                        .setMessage(R.string.alert_error_authenticate_twitter)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                dialog.dismiss()
            }
        })
    }

    /**
     * クリア情報を同期ボタン押下時の処理
     */
    override fun onClickSyncButton(view: View) {
        // ボタンを無効化
        binding.syncButton.isEnabled = false

        // クリア情報を同期
        syncClearDataInBackground()
    }

    /**
     * 音量領域の設定
     */
    override fun switchPlayable(view: View) {
        soundManager.togglePlayable()
        refresh()
    }

    override fun onClickPrivacyPolicy(view: View) {
        val uri = Uri.parse("https://my-android-server.appspot.com/html/privacy.html")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    @MainThread
    private fun sendAuthToken(authToken: TwitterAuthToken) {
        // サーバに認証情報を送信
        tumeKyouenService.login(authToken.token, authToken.secret)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<SingleSubscribeProxy<LoginResult>>(AutoDispose.autoDisposable<LoginResult>(AndroidLifecycleScopeProvider.from(this)))
                .subscribe({ s ->
                    // ログイン情報を保存
                    loginUtil.saveLoginInfo(authToken)
                    onSuccessTwitterAuth()
                },
                        { throwable -> onFailedTwitterAuth() })
    }

    /**
     * クリアステージデータの同期を行う。
     */
    private fun syncClearDataInBackground() {
        // クリアした情報を取得
        tumeKyouenRepository.selectAllClearStage()
                .subscribeOn(Schedulers.io())
                .flatMap<AddAllResponse> { stages -> ServerUtil.addAll(tumeKyouenService, stages) }
                .flatMapCompletable { addAllResponse ->
                    if (addAllResponse.data() == null) {
                        return@flatMapCompletable Completable.complete()
                    }
                    tumeKyouenRepository.updateSyncClearData(addAllResponse.data())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<CompletableSubscribeProxy>(AutoDispose.autoDisposable<Any>(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        {
                            enableSyncButton()
                            refresh()
                        },
                        { throwable ->
                            Timber.e(throwable, "クリア情報の送信に失敗")
                            enableSyncButton()
                        }
                )
    }

    /**
     * twitter連携に成功した場合の処理。
     * ボタンを切り替える。
     */
    @MainThread
    private fun onSuccessTwitterAuth() {
        binding.connectButton.isEnabled = false
        binding.connectButton.visibility = View.INVISIBLE
        binding.syncButton.visibility = View.VISIBLE
    }

    /**
     * twitter連携に失敗した場合の処理
     */
    @MainThread
    private fun onFailedTwitterAuth() {
        binding.connectButton.isEnabled = true
        loginUtil.saveLoginInfo(null)
        AlertDialog.Builder(this)
                .setMessage(R.string.alert_error_authenticate_twitter)
                .setPositiveButton(android.R.string.ok, null).show()
    }

    @MainThread
    private fun enableSyncButton() {
        // ボタンを有効化
        binding.syncButton.isEnabled = true
        refreshAll()
    }

    /**
     * 描画内容を再設定します。
     */
    private fun refreshAll() {
        refreshGetStageButton()
        refresh()
    }

    /**
     * ステージ取得ボタンを再設定します。
     */
    private fun refreshGetStageButton() {
        if (insertDataTask.running) {
            binding.getStageButton.isClickable = false
            binding.getStageButton.text = getString(R.string.get_more_loading)
        } else {
            binding.getStageButton.isClickable = true
            binding.getStageButton.text = getString(R.string.get_more)
        }
    }

    /**
     * ステージ数領域を再設定します。
     */
    private fun refresh() {
        tumeKyouenRepository.selectStageCount()
                .subscribeOn(Schedulers.io())
                .`as`<SingleSubscribeProxy<StageCountModel>>(AutoDispose.autoDisposable<StageCountModel>(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { stageCountModel -> binding.model = TitleActivityViewModel(this, stageCountModel, soundManager) }
                )
    }

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, TitleActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
