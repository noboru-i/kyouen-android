package hm.orz.chaos114.android.tumekyouen.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.CookieManager;

import javax.inject.Singleton;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.tumekyouen.App;
import hm.orz.chaos114.android.tumekyouen.R;
import hm.orz.chaos114.android.tumekyouen.db.AppDatabase;
import hm.orz.chaos114.android.tumekyouen.network.GsonAutoValueAdapterFactory;
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService;
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository;
import hm.orz.chaos114.android.tumekyouen.util.EncryptionUtil;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module
public class AppModule {

    @Provides
    @Singleton
    Context provideApplicationContext(App application) {
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
    AppDatabase provideAppDatabase(Context context) {
        return Room
                .databaseBuilder(
                        context,
                        AppDatabase.class,
                        "irokae.db"
                )
                .addMigrations(MIGRATION_2_3)
                .build();
    }

    @Provides
    TumeKyouenRepository provideTumeKyouenRepository(AppDatabase appDatabase) {
        return new TumeKyouenRepository(appDatabase);
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
                .registerTypeAdapterFactory(GsonAutoValueAdapterFactory.create())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(TumeKyouenService.class);
    }

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tume_kyouen RENAME TO old_tume_kyouen");
            database.execSQL("CREATE TABLE IF NOT EXISTS tume_kyouen (" +
                    " `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " `stage_no` INTEGER NOT NULL," +
                    " `size` INTEGER NOT NULL," +
                    " `stage` TEXT NOT NULL," +
                    " `creator` TEXT NOT NULL," +
                    " `clear_flag` INTEGER NOT NULL," +
                    " `clear_date` INTEGER NOT NULL" +
                    ")");
            database.execSQL("CREATE UNIQUE INDEX `index_tume_kyouen_stage_no` ON `tume_kyouen` (`stage_no`)");
            database.execSQL("DROP TABLE old_tume_kyouen");
        }
    };
}
