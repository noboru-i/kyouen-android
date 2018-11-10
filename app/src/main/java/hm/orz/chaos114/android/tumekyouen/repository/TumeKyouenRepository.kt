package hm.orz.chaos114.android.tumekyouen.repository

import hm.orz.chaos114.android.tumekyouen.db.AppDatabase
import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class TumeKyouenRepository(val appDatabase: AppDatabase) {
    fun insertByCSV(csvString: String) {
        val splitString = csvString.split(",".toRegex()).toTypedArray()
        if (splitString.size != 4) {
            throw RuntimeException("illegal csv: " + csvString)
        }

        var i = 0
        val entity = TumeKyouen(0,
                splitString[i++].toInt(),
                splitString[i++].toInt(),
                splitString[i++],
                splitString[i],
                0,
                0
        )
        appDatabase.tumeKyouenDao().insertAll(entity)
                .subscribe()
    }

    fun selectMaxStageNo(): Single<Int> {
        return appDatabase.tumeKyouenDao().selectMaxStageNo()
    }

    fun selectStageCount(): Single<StageCountModel> {
        return appDatabase.tumeKyouenDao().selectStageCount()
                .map { StageCountModel.create(it.count, it.clearCount) }
    }

    fun findStage(stageNo: Int): Single<TumeKyouenModel> {

        return appDatabase.tumeKyouenDao().findStage(stageNo)
                .map {
                    TumeKyouenModel.create(
                            it.stageNo,
                            it.size,
                            it.stage,
                            it.creator,
                            it.clearFlag,
                            Date(it.clearDate.toLong()))
                }
    }

    fun updateClearFlag(stageNo: Int, date: Date) {
        val dao = appDatabase.tumeKyouenDao()
        dao.findStage(stageNo)
                .subscribeOn(Schedulers.io())
                .doOnSuccess {
                    it.clearDate = date.time
                    dao.updateAll(it)
                            .subscribe()
                }
                .subscribe()
    }
}