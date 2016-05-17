package hm.orz.chaos114.android.tumekyouen.di;

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
    PreferenceUtil providePreferenceUtil(SharedPreferences sp) {
        return new PreferenceUtil(sp);
    }

    @Provides
    @Singleton
    LoginUtil provideLoginUtil(PreferenceUtil preferenceUtil, EncryptionUtil encryptionUtil) {
        return new LoginUtil(preferenceUtil, encryptionUtil);
    }

    @Provides
    @Singleton
    EncryptionUtil provideEncryptionUtil(PreferenceUtil preferenceUtil) {
        return new EncryptionUtil(preferenceUtil);
    }

    @Provides
    @Singleton
    SoundManager provideSoundManager(PreferenceUtil preferenceUtil, Context context) {
        return new SoundManager(preferenceUtil, context);
    }
}
