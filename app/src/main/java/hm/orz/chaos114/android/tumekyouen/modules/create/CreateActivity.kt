package hm.orz.chaos114.android.tumekyouen.modules.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R

class CreateActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun start(activity: Activity) {
            val intent = Intent(activity, CreateActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
    }
}
