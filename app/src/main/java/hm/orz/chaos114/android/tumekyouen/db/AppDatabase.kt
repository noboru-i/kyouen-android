package hm.orz.chaos114.android.tumekyouen.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hm.orz.chaos114.android.tumekyouen.db.dao.TumeKyouenDao
import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen

@Database(entities = arrayOf(TumeKyouen::class), version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tumeKyouenDao(): TumeKyouenDao
}
