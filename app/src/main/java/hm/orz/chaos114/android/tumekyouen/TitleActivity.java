package hm.orz.chaos114.android.tumekyouen;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

@WindowFeature({ Window.FEATURE_NO_TITLE, Window.FEATURE_INDETERMINATE_PROGRESS })
@EActivity(R.layout.activity_title)
public class TitleActivity extends FragmentActivity {

    @ViewById
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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
}
