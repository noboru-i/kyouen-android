package hm.orz.chaos114.android.tumekyouen;

import android.app.Application;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Application class。
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_key),
                getString(R.string.twitter_secret)
        );
        Fabric.with(this, new TwitterCore(authConfig));
    }
}
