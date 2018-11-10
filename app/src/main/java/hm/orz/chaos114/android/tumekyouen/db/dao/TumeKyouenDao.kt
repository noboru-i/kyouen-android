package hm.orz.chaos114.android.tumekyouen.db.dao

import androidx.room.*
import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TumeKyouenDao {
    @Insert
    fun insertAll(vararg tumeKyouens: TumeKyouen): Completable

    @Update
    fun updateAll(vararg tumeKyouens: TumeKyouen): Completable

    @Query("SELECT * FROM tume_kyouen WHERE stage_no = :stageNo")
    fun findStage(stageNo: Int): Single<TumeKyouen>

    @Query("SELECT MAX(stage_no) FROM tume_kyouen")
    fun selectMaxStageNo(): Single<Int>

    data class CountTuple(
            @ColumnInfo(name = "count") var count: Int,
            @ColumnInfo(name = "clear_count") var clearCount: Int
    )

    @Query("SELECT COUNT(*) AS count, SUM(clear_flag) AS clear_count FROM tume_kyouen")
    fun selectStageCount(): Single<CountTuple>
}