package hm.orz.chaos114.android.tumekyouen.modules.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityCreateBinding
import hm.orz.chaos114.android.tumekyouen.model.KyouenData
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class CreateActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var soundManager: SoundManager

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
        binding.kyouenView.inject(soundManager, ::onKyouen)
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.backOneStepButton.setOnClickListener { backOneStep() }
        binding.resetButton.setOnClickListener { reset() }
        binding.sendStageButton.setOnClickListener { sendStage() }
    }

    private fun backOneStep() {
        binding.kyouenView.popStone()
        binding.overlayView.visibility = View.GONE
    }

    private fun reset() {
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.overlayView.visibility = View.GONE
    }

    private fun sendStage() {
        // TODO
    }

    private fun onKyouen(kyouenData: KyouenData) {
        binding.overlayView.visibility = View.VISIBLE
        binding.overlayView.setData(6, kyouenData)

        if (binding.kyouenView.getGameModel().blackStoneCount == 4) {
            AlertDialog.Builder(this)
                    .setTitle("Kyouen!!")
                    .setPositiveButton("ok", null)
                    .show()
            return
        }

        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
                .setTitle("Kyouen!!")
                .setMessage("下記の名前で石の配置を送信します。")
                .setView(editText)
                .setPositiveButton("ok") { dialog, which ->
                    val name = editText.text
                    Timber.d("name: %s", name)
                    // TODO send stage
                }
                .setNegativeButton("Cancel", null)
                .show()
    }
}
