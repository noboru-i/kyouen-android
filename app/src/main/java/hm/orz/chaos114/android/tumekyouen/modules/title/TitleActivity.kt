package hm.orz.chaos114.android.tumekyouen.modules.title

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.app.StageGetDialog
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityTitleBinding
import hm.orz.chaos114.android.tumekyouen.modules.create.CreateActivity
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.util.AdRequestFactory
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil
import hm.orz.chaos114.android.tumekyouen.util.setupAlertDialog
import hm.orz.chaos114.android.tumekyouen.util.setupToast
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class TitleActivity : DaggerAppCompatActivity(), TitleActivityHandlers {
    @Inject
    internal lateinit var preferenceUtil: PreferenceUtil
    @Inject
    internal lateinit var tumeKyouenRepository: TumeKyouenRepository

    @Inject
    lateinit var factory: TitleViewModelFactory

    private val viewModel: TitleViewModel by lazy {
        ViewModelProvider(this, factory).get(TitleViewModel::class.java)
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

        dialog = ProgressDialog(this)

        viewModel.onCreate()

        viewModel.showLoading.observe(this, Observer { showLoading ->
            Timber.d("showLoading: %s", showLoading)
            if (showLoading) {
                dialog.setMessage("Now Loading...")
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                dialog.show()
            } else {
                dialog.dismiss()
            }
        })

        binding.root.setupAlertDialog(this, viewModel.alertMessage)
        binding.root.setupToast(this, viewModel.toastMessage)

        viewModel.refresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.onActivityResult(requestCode, resultCode, data)

        viewModel.refresh()
    }

    override fun onClickStartButton(view: View) {
        // TODO make router class
        val stageNo = lastStageNo
        tumeKyouenRepository.findStage(stageNo)
            .subscribeOn(Schedulers.io())
            .autoDisposable(scopeProvider)
            .subscribe { item ->
                KyouenActivity.start(this, item)
            }
    }

    override fun onClickGetStage(v: View) {
        val dialog = StageGetDialog(this,
            object : StageGetDialog.OnSuccessListener {
                override fun onSuccess(count: Int) {
                    viewModel.requestStages(count)
                }
            },
            DialogInterface.OnCancelListener { viewModel.refresh() })
        dialog.show()
    }

    override fun onClickCreateStage(v: View) {
        CreateActivity.start(this)
    }

    override fun onClickConnectButton(view: View) {
        viewModel.requestConnectTwitter(this)
    }

    override fun onClickSyncButton(view: View) {
        viewModel.requestSync()
    }

    override fun switchPlayable(view: View) {
        viewModel.toggleSoundPlayable()
    }

    override fun onClickPrivacyPolicy(view: View) {
        val uri = Uri.parse("https://my-android-server.appspot.com/html/privacy.html")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}
