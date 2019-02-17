package hm.orz.chaos114.android.tumekyouen.modules.title

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityTitleBinding
import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

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

    @Inject
    lateinit var factory: TitleViewModelFactory

    private val viewModel: TitleViewModel by lazy {
        ViewModelProviders.of(this, factory).get(TitleViewModel::class.java)
    }

    private lateinit var binding: ActivityTitleBinding

    private lateinit var dialog: ProgressDialog

    private val lastStageNo: Int
        get() {
            var lastStageNo = preferenceUtil.getInt(PreferenceUtil.KEY_LAST_STAGE_NO)
            if (lastStageNo == 0) {
                lastStageNo = 1
            }

            return lastStageNo
        }

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, TitleActivity::class.java)
            activity.startActivity(intent)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_title)
        binding.lifecycleOwner = this
        binding.handlers = this
        binding.model = viewModel

        volumeControlStream = AudioManager.STREAM_MUSIC

        binding.adView.loadAd(AdRequestFactory.createAdRequest())

        dialog = ProgressDialog(this@TitleActivity)

        viewModel.onCreate()

        viewModel.showLoading.observe(this, Observer { showLoading ->
            if (showLoading) {
                dialog.setMessage("Now Loading...")
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                dialog.show()
            } else {
                dialog.dismiss()
            }
        })

        refreshAll()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.onActivityResult(requestCode, resultCode, data)

        refreshAll()
    }

    override fun onClickStartButton(view: View) {
        val stageNo = lastStageNo
        tumeKyouenRepository.findStage(stageNo)
                .subscribeOn(Schedulers.io())
                .autoDisposable(scopeProvider)
                .subscribe { item ->
                    KyouenActivity.start(this, item)
                }
    }

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
                                .autoDisposable(scopeProvider)
                                .subscribe(
                                        { successCount ->
                                            Toast.makeText(this@TitleActivity,
                                                    getString(R.string.toast_get_stage, successCount),
                                                    Toast.LENGTH_SHORT).show()
                                            viewModel.refresh()
                                        },
                                        {
                                            Toast.makeText(this@TitleActivity,
                                                    R.string.toast_no_stage,
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                )
                    }
                },
                DialogInterface.OnCancelListener { refreshAll() })
        dialog.show()
    }

    override fun onClickCreateStage(v: View) {
        CreateActivity.start(this)
    }

    override fun onClickConnectButton(view: View) {
        // FIXME refactor
        viewModel.requestConnectTwitter(this)
    }

    override fun onClickSyncButton(view: View) {
        binding.syncButton.isEnabled = false
        syncClearDataInBackground()
    }

    override fun switchPlayable(view: View) {
        viewModel.toggleSoundPlayable()
    }

    override fun onClickPrivacyPolicy(view: View) {
        val uri = Uri.parse("https://my-android-server.appspot.com/html/privacy.html")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun syncClearDataInBackground() {
        tumeKyouenRepository.selectAllClearStage()
                .subscribeOn(Schedulers.io())
                .flatMap<AddAllResponse> { stages -> ServerUtil.addAll(tumeKyouenService, stages) }
                .flatMapCompletable { addAllResponse ->
                    tumeKyouenRepository.updateSyncClearData(addAllResponse.data)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scopeProvider)
                .subscribe(
                        {
                            enableSyncButton()
                            viewModel.refresh()
                        },
                        { throwable ->
                            Timber.e(throwable, "クリア情報の送信に失敗")
                            enableSyncButton()
                        }
                )
    }

    @MainThread
    private fun enableSyncButton() {
        binding.syncButton.isEnabled = true
        refreshAll()
    }

    private fun refreshAll() {
        refreshGetStageButton()
        viewModel.refresh()
    }

    private fun refreshGetStageButton() {
        if (insertDataTask.running) {
            binding.getStageButton.isClickable = false
            binding.getStageButton.text = getString(R.string.get_more_loading)
        } else {
            binding.getStageButton.isClickable = true
            binding.getStageButton.text = getString(R.string.get_more)
        }
    }
}
