package hm.orz.chaos114.android.tumekyouen.modules.create

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityCreateBinding
import hm.orz.chaos114.android.tumekyouen.model.KyouenData
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class CreateActivity : DaggerAppCompatActivity(), CreateKyouenView.CreateKyouenViewListener {
    @Inject
    lateinit var preferenceUtil: PreferenceUtil
    @Inject
    lateinit var soundManager: SoundManager

    @Inject
    lateinit var tumeKyouenService: TumeKyouenService

    private val binding: ActivityCreateBinding by lazy {
        DataBindingUtil.setContentView<ActivityCreateBinding>(this, R.layout.activity_create)
    }

    companion object {
        @JvmStatic
        fun start(activity: Activity) {
            val intent = Intent(activity, CreateActivity::class.java)
            activity.startActivity(intent)
        }

        val INITIAL_STAGE_6 = TumeKyouenModel.create(0, 6, "000000000000000000000000000000", "", 0, Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.kyouenView.inject(soundManager, this)
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.backOneStepButton.setOnClickListener { backOneStep() }
        binding.resetButton.setOnClickListener { reset() }
        binding.sendStageButton.setOnClickListener { handleSendStage() }

        applyButtonState()
    }

    // region CreateKyouenView.CreateKyouenViewListener
    override fun onKyouen(kyouenData: KyouenData) {
        binding.overlayView.visibility = View.VISIBLE
        binding.overlayView.setData(6, kyouenData)

        if (binding.kyouenView.getGameModel().blackStoneCount == 4) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.create_send_title)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            return
        }

        confirmSendName()
    }

    override fun onAddStone() {
        applyButtonState()
    }
    // endregion

    private fun backOneStep() {
        binding.kyouenView.popStone()
        binding.overlayView.visibility = View.GONE
        applyButtonState()
    }

    private fun reset() {
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.overlayView.visibility = View.GONE
        applyButtonState()
    }

    private fun handleSendStage() {
        confirmSendName()
    }

    private fun applyButtonState() {
        val hasStone = binding.kyouenView.gameModel.blackStoneCount > 0
        binding.backOneStepButton.isEnabled = hasStone
        binding.resetButton.isEnabled = hasStone

        binding.sendStageButton.isEnabled = binding.kyouenView.gameModel.hasKyouen() != null
    }

    private fun confirmSendName() {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.setText(preferenceUtil.getString(PreferenceUtil.KEY_CREATOR_NAME), TextView.BufferType.NORMAL)
        AlertDialog.Builder(this)
                .setTitle(R.string.create_send_title)
                .setMessage(R.string.create_send_message)
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val name = editText.text.toString()
                    preferenceUtil.putString(PreferenceUtil.KEY_CREATOR_NAME, name)
                    Timber.d("name: %s", name)
                    sendState(name)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun sendState(creator: String) {
        val size = binding.kyouenView.gameModel.size()
        val stage = binding.kyouenView.gameModel.getStageStateForSend()
        val data = TextUtils.join(",", arrayOf(size, stage, creator))
        Timber.d("data = %s", data)

        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.create_send_loading))
            setProgressStyle(ProgressDialog.STYLE_SPINNER);
            show();
        }

        tumeKyouenService.postStage(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        { response ->
                            val responseString = response.body()?.string()
                            Timber.d("sucess : %s", responseString);
                            progressDialog.dismiss()

                            val message = convertResponseToMessage(responseString)
                            AlertDialog.Builder(this)
                                    .setMessage(message)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                        },
                        { throwable ->
                            Timber.d(throwable, "fail");
                            AlertDialog.Builder(this)
                                    .setMessage(R.string.create_result_failure)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                        }
                )
    }

    private fun convertResponseToMessage(response: String?): String {
        if (response == null) {
            return getString(R.string.create_result_failure)
        }

        val matchEntire = "success stageNo=([0-9]*)".toRegex().matchEntire(response)
        if (matchEntire == null) {
            if (response.equals("registered")) {
                return getString(R.string.create_result_registered)
            }
            return getString(R.string.create_result_failure)
        }

        return matchEntire.destructured.let { (stageNo) ->
            getString(R.string.create_send_success_message, stageNo)
        }
    }
}
