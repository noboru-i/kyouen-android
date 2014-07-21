package hm.orz.chaos114.android.tumekyouen;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;
import org.androidannotations.annotations.rest.RestService;

import java.io.IOException;

import hm.orz.chaos114.android.tumekyouen.network.TumekyouenClient;
import hm.orz.chaos114.android.tumekyouen.network.entity.RegistrationId;

@WindowFeature({Window.FEATURE_NO_TITLE, Window.FEATURE_INDETERMINATE_PROGRESS})
@EActivity(R.layout.activity_title)
public class TitleActivity extends FragmentActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @ViewById
    AdView adView;

    @RestService
    TumekyouenClient tumekyouenClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @AfterViews
    void getGcm() {
        if (checkPlayServices()) {
            getGcmRegistrationId();
        }
    }

    @Background
    void getGcmRegistrationId() {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        try {
            String regId = gcm.register(getString(R.string.gcm_sender_id));
            Log.d("TEST", "regId = " + regId);
            RegistrationId registrationId = new RegistrationId(regId);
            tumekyouenClient.registGcm(registrationId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Click(R.id.start_button)
    void clickStart() {
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.get_stage_button)
    void clickGetStage() {
        Toast.makeText(this, "ステージ取得", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.create_stage_button)
    void clickCreateStage() {
        Toast.makeText(this, "ステージ作成", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.connect_button)
    void clickConnect() {
        Toast.makeText(this, "twitterでログイン", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.sync_button)
    void clickSync() {
        Toast.makeText(this, "同期", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("TEST", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
