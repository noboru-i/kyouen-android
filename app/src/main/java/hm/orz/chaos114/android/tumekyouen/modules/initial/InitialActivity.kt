package hm.orz.chaos114.android.tumekyouen.modules.initial

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import dagger.android.support.DaggerAppCompatActivity
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class InitialActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var tumeKyouenRepository: TumeKyouenRepository

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        tumeKyouenRepository.selectMaxStageNo()
                .subscribeOn(Schedulers.io())
                .autoDisposable(scopeProvider)
                .subscribe(
                        {
                            // has stages in db
                            goNextActivity()
                        },
                        {
                            insertInitialDataInBackground()
                            goNextActivity()
                        })
    }

    @WorkerThread
    private fun insertInitialDataInBackground() {
        val initData = arrayOf(
                "1,6,000000010000001100001100000000001000,noboru",
                "2,6,000000000000000100010010001100000000,noboru",
                "3,6,000000001000010000000100010010001000,noboru",
                "4,6,001000001000000010010000010100000000,noboru",
                "5,6,000000001011010000000010001000000010,noboru",
                "6,6,000100000000101011010000000000000000,noboru",
                "7,6,000000001010000000010010000000001010,noboru",
                "8,6,001000000001010000010010000001000000,noboru",
                "9,6,000000001000010000000010000100001000,noboru",
                "10,6,000100000010010000000100000010010000,noboru"
        )
        for (data in initData) {
            tumeKyouenRepository.insertByCSV(data).blockingAwait()
        }
    }

    private fun goNextActivity() {
        val uri = intent.data
        if (uri == null) {
            // open without url
            goToTitle()
            return
        }

        val stageNo = getStageNumberFromUri(uri)
        if (stageNo == null) {
            // cannot get stage number from url.
            goToTitle()
            return
        }

        tumeKyouenRepository.findStage(stageNo)
                .autoDisposable(scopeProvider)
                .subscribe(
                        { item ->
                            KyouenActivity.start(this, item)
                            finish()
                        },
                        {
                            // cannot get stage info from local DB.
                            Toast.makeText(this, R.string.toast_fetch_first, Toast.LENGTH_LONG).show()
                            goToTitle()
                        }
                )
    }

    private fun goToTitle() {
        TitleActivity.start(this@InitialActivity)
        finish()
    }

    private fun getStageNumberFromUri(uri: Uri): Int? {
        val open = uri.getQueryParameter("open") ?: return null

        try {
            return Integer.valueOf(open)
        } catch (e: NumberFormatException) {
            return null
        }

    }
}
