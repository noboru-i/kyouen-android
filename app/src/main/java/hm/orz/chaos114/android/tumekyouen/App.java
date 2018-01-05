package hm.orz.chaos114.android.tumekyouen;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.di.AppModule;
import hm.orz.chaos114.android.tumekyouen.di.DaggerAppComponent;
import lombok.Getter;
import timber.log.Timber;

/**
 * Application class。
 */
public class App extends Application {

    @Getter
    private AppComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

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

        applicationComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    private static final class FirebaseTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            Crashlytics.log(priority, tag, message);
            if (t != null && priority >= Log.ERROR) {
                Crashlytics.logException(t);
            }
        }
    }
}
