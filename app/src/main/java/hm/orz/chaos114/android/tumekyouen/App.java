package hm.orz.chaos114.android.tumekyouen;

import android.app.Application;

import com.deploygate.sdk.DeployGate;

/**
 * Created by ishikuranoboru on 2014/07/22.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DeployGate.install(this);
    }
}
