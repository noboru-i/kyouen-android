package hm.orz.chaos114.android.tumekyouen.util;

import android.os.Bundle;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

public final class AdRequestFactory {
    private AdRequestFactory() {
        // prevent instantiate
    }

    public static AdRequest createAdRequest() {
        Bundle extras = new Bundle();
        extras.putString("max_ad_content_rating", "G");
        return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
    }
}
