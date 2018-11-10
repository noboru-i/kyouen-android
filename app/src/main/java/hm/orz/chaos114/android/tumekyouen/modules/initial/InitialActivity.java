package hm.orz.chaos114.android.tumekyouen.modules.initial;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import javax.inject.Inject;

import androidx.annotation.WorkerThread;
import dagger.android.support.DaggerAppCompatActivity;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * 初期化処理Activity。
 */
public class InitialActivity extends DaggerAppCompatActivity {

    @Inject
    TumeKyouenRepository tumeKyouenRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        tumeKyouenRepository.selectMaxStageNo()
                .subscribeOn(Schedulers.io())
                .subscribe(stageNo -> {
                            // has stages in db
                            Timber.d("!!!stageNo");
                            goNextActivity();
                        },
                        throwable -> {
                            Timber.d(throwable, "!!!");
                            insertInitialDatatInBackground();
                            goNextActivity();
                        });
    }

    /**
     * 初期データを登録する。
     */
    @WorkerThread
    private void insertInitialDatatInBackground() {
        final String[] initData = new String[]{
                "1,6,000000010000001100001100000000001000,noboru",
                "2,6,000000000000000100010010001100000000,noboru",
                "3,6,000000001000010000000100010010001000,noboru",
                "4,6,001000001000000010010000010100000000,noboru",
                "5,6,000000001011010000000010001000000010,noboru",
                "6,6,000100000000101011010000000000000000,noboru",
                "7,6,000000001010000000010010000000001010,noboru",
                "8,6,001000000001010000010010000001000000,noboru",
                "9,6,000000001000010000000010000100001000,noboru",
                "10,6,000100000010010000000100000010010000,noboru"};
        for (final String data : initData) {
            tumeKyouenRepository.insertByCSV(data);
        }
    }

    private void goNextActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            // open without url
            goToTitle();
            return;
        }

        final Integer stageNo = getStageNumberFromUri(uri);
        if (stageNo == null) {
            // cannot get stage number from url.
            goToTitle();
            return;
        }

        tumeKyouenRepository.findStage(stageNo)
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        item -> {
                            KyouenActivity.start(this, item);
                            finish();
                        },
                        throwable -> {
                            // cannot get stage info from local DB.
                            Toast.makeText(this, R.string.toast_fetch_first, Toast.LENGTH_LONG).show();
                            goToTitle();
                        }
                );
    }

    private void goToTitle() {
        TitleActivity.start(InitialActivity.this);
        finish();
    }

    private Integer getStageNumberFromUri(Uri uri) {
        String open = uri.getQueryParameter("open");
        if (open == null) {
            return null;
        }

        try {
            return Integer.valueOf(open);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
