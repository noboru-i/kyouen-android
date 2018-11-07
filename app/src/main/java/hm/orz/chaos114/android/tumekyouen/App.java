package hm.orz.chaos114.android.tumekyouen;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import org.jetbrains.annotations.NotNull;

import androidx.multidex.MultiDex;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import hm.orz.chaos114.android.tumekyouen.di.AppModule;
import hm.orz.chaos114.android.tumekyouen.di.DaggerAppComponent;
import timber.log.Timber;

/**
 * Application class。
 */
public class App extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this, getString(R.string.admob_app_id));

        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_key),
                getString(R.string.twitter_secret)
        );
        TwitterConfig config = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();
        Twitter.initialize(config);

        if (!FirebaseApp.getApps(this).isEmpty()) {
            // only execute foreground process
            FirebaseMessaging.getInstance().subscribeToTopic("all");
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new FirebaseTree());
        }

    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder()
                .application(this)
                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private static final class FirebaseTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
            Crashlytics.log(priority, tag, message);
            if (t != null && priority >= Log.ERROR) {
                Crashlytics.logException(t);
            }
        }
    }
}
