package hm.orz.chaos114.android.tumekyouen.shadows;

import com.google.android.gms.ads.AdView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(AdView.class)
public class ShadowAdView extends ShadowViewGroup {
    @Implementation
    public void loadAd(com.google.android.gms.ads.AdRequest adRequest) {
    }
}
