package hm.orz.chaos114.android.tumekyouen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hm.orz.chaos114.android.tumekyouen.network.TumekyouenClient;
import hm.orz.chaos114.android.tumekyouen.network.entity.Response;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

public class TitleActivity extends FragmentActivity {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @InjectView(R.id.title_ad_view)
    AdView mAdView;

    TumekyouenClient tumekyouenClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_title);

        ButterKnife.inject(this);

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://my-android-server.appspot.com")
                .setConverter(new GsonConverter(gson))
                .build();

        tumekyouenClient = restAdapter.create(TumekyouenClient.class);

        loadAd();
        getGcm();
    }

    void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    void getGcm() {
        if (checkPlayServices()) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    getGcmRegistrationId();
                    return null;
                }
            }.execute();
        }
    }

    void getGcmRegistrationId() {
        Log.d("TEST", "unit_id=" + getString(R.string.unit_id));
        Log.d("TEST", "twitter_key=" + getString(R.string.twitter_key));
        Log.d("TEST", "twitter_secret=" + getString(R.string.twitter_secret));
        Log.d("TEST", "bug_sense_api_key=" + getString(R.string.bug_sense_api_key));
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        try {
            Log.d("TEST", "sender_id=" + getString(R.string.gcm_sender_id));
            String regId = gcm.register(getString(R.string.gcm_sender_id));
            Log.d("TEST", "regId = " + regId);
            tumekyouenClient.registGcm(regId, new Callback<Response>() {
                @Override
                public void success(Response response, retrofit.client.Response response2) {
                    Log.d("TEST", "response = " + response);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("TEST", "/gcm/regist is faled.");
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnClick(R.id.start_button)
    void clickStart() {
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.get_stage_button)
    void clickGetStage() {
        Toast.makeText(this, "ステージ取得", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.create_stage_button)
    void clickCreateStage() {
        Toast.makeText(this, "ステージ作成", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.connect_button)
    void clickConnect() {
        Toast.makeText(this, "twitterでログイン", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sync_button)
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
