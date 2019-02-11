package hm.orz.chaos114.android.tumekyouen.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.net.CookieManager

import javax.inject.Singleton

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.tumekyouen.App
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.db.AppDatabase
import hm.orz.chaos114.android.tumekyouen.network.GsonAutoValueAdapterFactory
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.EncryptionUtil
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@Module
class AppModule {

    @Provides
    @Singleton
    internal fun provideApplicationContext(application: App): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    internal fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    @Provides
    @Singleton
    internal fun providePreferenceUtil(sp: SharedPreferences): PreferenceUtil {
        return PreferenceUtil(sp)
    }

    @Provides
    @Singleton
    internal fun provideLoginUtil(preferenceUtil: PreferenceUtil, encryptionUtil: EncryptionUtil): LoginUtil {
        return LoginUtil(preferenceUtil, encryptionUtil)
    }

    @Provides
    @Singleton
    internal fun provideEncryptionUtil(preferenceUtil: PreferenceUtil): EncryptionUtil {
        return EncryptionUtil(preferenceUtil)
    }

    @Provides
    @Singleton
    internal fun provideSoundManager(preferenceUtil: PreferenceUtil, context: Context): SoundManager {
        return SoundManager(preferenceUtil, context)
    }

    @Provides
    internal fun provideAppDatabase(context: Context): AppDatabase {
        return Room
                .databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "irokae.db"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
    }

    @Provides
    internal fun provideTumeKyouenRepository(appDatabase: AppDatabase): TumeKyouenRepository {
        return TumeKyouenRepository(appDatabase)
    }

    @Provides
    @Singleton
    internal fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    internal fun provideTumeKyouenService(context: Context): TumeKyouenService {
        val logging = HttpLoggingInterceptor { message -> Timber.tag("OkHttp").d(message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .cookieJar(JavaNetCookieJar(CookieManager()))
                .build()

        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapterFactory(GsonAutoValueAdapterFactory.create())
                .create()
        val retrofit = Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return retrofit.create(TumeKyouenService::class.java)
    }

    @Provides
    @Singleton
    internal fun provideInsertDataTask(tumeKyouenService: TumeKyouenService,
                                       tumeKyouenRepository: TumeKyouenRepository): InsertDataTask {
        return InsertDataTask(tumeKyouenService, tumeKyouenRepository)
    }

    companion object {

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tume_kyouen RENAME TO old_tume_kyouen")
                database.execSQL("CREATE TABLE IF NOT EXISTS tume_kyouen (" +
                        " `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        " `stage_no` INTEGER NOT NULL," +
                        " `size` INTEGER NOT NULL," +
                        " `stage` TEXT NOT NULL," +
                        " `creator` TEXT NOT NULL," +
                        " `clear_flag` INTEGER NOT NULL," +
                        " `clear_date` INTEGER NOT NULL" +
                        ")")
                database.execSQL("CREATE UNIQUE INDEX `index_tume_kyouen_stage_no` ON `tume_kyouen` (`stage_no`)")
                database.execSQL("INSERT INTO tume_kyouen" + " SELECT _id, stage_no, size, stage, creator, clear_flag, clear_date FROM old_tume_kyouen")
                database.execSQL("DROP TABLE old_tume_kyouen")
            }
        }
    }
}
