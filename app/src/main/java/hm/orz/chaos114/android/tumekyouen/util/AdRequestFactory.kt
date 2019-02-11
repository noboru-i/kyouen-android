package hm.orz.chaos114.android.tumekyouen.util

import android.os.Bundle

import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

object AdRequestFactory {

    fun createAdRequest(): AdRequest {
        val extras = Bundle()
        extras.putString("max_ad_content_rating", "G")
        return AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
    }
}
