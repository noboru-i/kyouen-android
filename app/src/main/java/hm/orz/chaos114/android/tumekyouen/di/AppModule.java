package hm.orz.chaos114.android.tumekyouen.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.util.EncryptionUtil;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

@Module
public class AppModule {
    private final App application;

    public AppModule(App application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Provides
    @Singleton
    PreferenceUtil providePreferenceUtil() {
        return new PreferenceUtil(application);
    }

    @Provides
    @Singleton
    LoginUtil provideLoginUtil() {
        return new LoginUtil(application);
    }

    @Provides
    @Singleton
    EncryptionUtil provideEncryptionUtil() {
        return new EncryptionUtil(application);
    }

    @Provides
    @Singleton
    SoundManager provideSoundManager() {
        return new SoundManager(application);
    }
}
