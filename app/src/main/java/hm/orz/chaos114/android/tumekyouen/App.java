package hm.orz.chaos114.android.tumekyouen;

import android.app.Application;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import hm.orz.chaos114.android.tumekyouen.di.AppComponent;
import hm.orz.chaos114.android.tumekyouen.di.AppModule;
import hm.orz.chaos114.android.tumekyouen.di.DaggerAppComponent;
import io.fabric.sdk.android.Fabric;
import lombok.Getter;

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

        applicationComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }
}
