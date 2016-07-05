package hm.orz.chaos114.android.tumekyouen;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.di.AppModule;
import hm.orz.chaos114.android.tumekyouen.di.DaggerAppComponent;
import io.fabric.sdk.android.Fabric;
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
        Fabric.with(this, new TwitterCore(authConfig));

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

    private final static class FirebaseTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            FirebaseCrash.logcat(priority, tag, message);
            if (t != null && priority >= Log.ERROR) {
                FirebaseCrash.report(t);
            }
        }
    }
}
