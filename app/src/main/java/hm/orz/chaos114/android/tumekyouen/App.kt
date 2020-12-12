package hm.orz.chaos114.android.tumekyouen

import android.content.Context
import android.util.Log

import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig

import androidx.multidex.MultiDex
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import hm.orz.chaos114.android.tumekyouen.di.DaggerAppComponent
import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil
import timber.log.Timber

class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this, getString(R.string.admob_app_id))

        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_key),
            getString(R.string.twitter_secret)
        )
        val config = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()
        Twitter.initialize(config)

        if (!FirebaseApp.getApps(this).isEmpty()) {
            // only execute foreground process
            FirebaseMessaging.getInstance().subscribeToTopic("all")
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FirebaseTree())
        }

        NotificationUtil.setup(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private class FirebaseTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            FirebaseCrashlytics.getInstance().log("${priority}/${tag}: ${message}")
            if (t != null && priority >= Log.ERROR) {
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        }
    }
}
