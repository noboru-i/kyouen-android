package hm.orz.chaos114.android.tumekyouen.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.tumekyouen.App
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.db.AppDatabase
import hm.orz.chaos114.android.tumekyouen.network.AuthInterceptor
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenV2Service
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Singleton

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
    @Singleton
    internal fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    internal fun provideTumeKyouenV2Service(context: Context): TumeKyouenV2Service {
        val logging = HttpLoggingInterceptor { message -> Timber.tag("OkHttp").d(message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor())
            .build()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_v2_url))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(TumeKyouenV2Service::class.java)
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
