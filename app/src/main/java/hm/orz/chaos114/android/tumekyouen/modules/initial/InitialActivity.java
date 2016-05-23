package hm.orz.chaos114.android.tumekyouen.modules.initial;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.GCMIntentService;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;

/**
 * 初期化処理Activity。
 */
public class InitialActivity extends AppCompatActivity {

    @Inject
    KyouenDb kyouenDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        getApplicationComponent().inject(this);

        // GCMへの登録
        registGcm();

        if (kyouenDb.selectMaxStageNo() == 0) {
            // データが存在しない場合

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    insertInitialDatatInBackground();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    TitleActivity.start(InitialActivity.this);
                    finish();
                }
            }.execute();
            return;
        }

        TitleActivity.start(InitialActivity.this);
        finish();
    }

    @Override
    protected void onDestroy() {
        GCMRegistrar.onDestroy(getApplicationContext());
        super.onDestroy();
    }

    /**
     * 初期データを登録する。
     */
    @WorkerThread
    void insertInitialDatatInBackground() {
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
            kyouenDb.insert(data);
        }
    }

    private AppComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }

    private void registGcm() {
        try {
            GCMRegistrar.checkDevice(getApplicationContext());
            GCMRegistrar.checkManifest(getApplicationContext());
        } catch (final UnsupportedOperationException e) {
            Log.e("kyouen", "unsupported gcm.", e);
            return;
        }
        final String regId = GCMRegistrar.getRegistrationId(getApplicationContext());
        Log.i("kyouen", "regId=" + regId);
        if (regId.length() == 0) {
            // GCMに登録
            GCMRegistrar.register(getApplicationContext(), GCMIntentService.getSenderId(this));
            return;
        }
        if (GCMRegistrar.isRegisteredOnServer(getApplicationContext())) {
            // 既に登録されている場合、終了
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                final boolean registered = ServerUtil.registGcm(InitialActivity.this, regId);
                // At this point all attempts to register with the app
                // server failed, so we need to unregister the device
                // from GCM - the app will try to register again when
                // it is restarted. Note that GCM will send an
                // unregistered callback upon completion, but
                // GCMIntentService.onUnregistered() will ignore it.
                if (!registered) {
                    GCMRegistrar.unregister(InitialActivity.this);
                }
                return null;
            }
        }.execute(null, null, null);
    }
}
