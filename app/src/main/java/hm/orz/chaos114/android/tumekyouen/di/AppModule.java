package hm.orz.chaos114.android.tumekyouen.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.CookieManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.KyouenDb;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import hm.orz.chaos114.android.tumekyouen.util.EncryptionUtil;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

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

    @Provides
    KyouenDb provideKyouenDb(Context context) {
        return new KyouenDb(context);
    }

    @Provides
    @Singleton
    FirebaseAnalytics provideFirebaseAnalytics(Context context) {
        return FirebaseAnalytics.getInstance(context);
    }

    @Provides
    @Singleton
    TumeKyouenService provideTumeKyouenService(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cookieJar(new JavaNetCookieJar(new CookieManager()))
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(TumeKyouenService.class);
    }
}
