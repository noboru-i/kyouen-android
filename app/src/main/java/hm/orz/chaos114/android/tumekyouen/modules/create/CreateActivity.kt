package hm.orz.chaos114.android.tumekyouen.modules.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.databinding.ActivityCreateBinding
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
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
        binding.kyouenView.inject(soundManager) {
            binding.overlayView.visibility = View.VISIBLE
            binding.overlayView.setData(6, it);
        }
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.resetButton.setOnClickListener { reset() }
    }

    private fun reset() {
        binding.kyouenView.setData(INITIAL_STAGE_6)
        binding.overlayView.visibility = View.GONE
    }
}
