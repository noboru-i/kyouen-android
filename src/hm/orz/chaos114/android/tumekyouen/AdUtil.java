package hm.orz.chaos114.android.tumekyouen;

import android.app.Activity;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdUtil {

	/**
	 * 広告を追加します。
	 * 
	 * @param activity 親アクティビティ
	 * @param layout 追加する対象
	 */
	public static void addAdView(Activity activity, LinearLayout layout) {
		// Create the adView
		AdView adView = new AdView(activity, AdSize.BANNER, "a14e2eb76fbaa7d");
		layout.addView(adView, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		// Initiate a generic request to load it with an ad
		AdRequest adRequest = new AdRequest();
		adView.loadAd(adRequest);
	}
}
