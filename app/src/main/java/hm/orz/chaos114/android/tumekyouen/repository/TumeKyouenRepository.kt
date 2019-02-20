package hm.orz.chaos114.android.tumekyouen.repository

import hm.orz.chaos114.android.tumekyouen.db.AppDatabase
import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen
import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TumeKyouenRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun insertByCSV(csvString: String): Completable {
        val splitString = csvString.split(",".toRegex()).toTypedArray()
        if (splitString.size != 4) {
            throw RuntimeException("illegal csv: $csvString")
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
        return appDatabase.tumeKyouenDao().insertAll(entity)
    }

    fun selectMaxStageNo(): Single<Int> {
        return appDatabase.tumeKyouenDao().selectMaxStageNo()
    }

    fun selectStageCount(): Single<StageCountModel> {
        return appDatabase.tumeKyouenDao().selectStageCount()
            .map { StageCountModel(it.count, it.clearCount) }
    }

    fun findStage(stageNo: Int): Maybe<TumeKyouenModel> {

        return appDatabase.tumeKyouenDao().findStage(stageNo)
            .map {
                TumeKyouenModel(
                    it.stageNo,
                    it.size,
                    it.stage,
                    it.creator,
                    it.clearFlag,
                    Date(it.clearDate))
            }
    }

    fun selectAllClearStage(): Single<List<TumeKyouenModel>> {
        return appDatabase.tumeKyouenDao().selectAllClearStage()
            .map {
                it.map { tumeKyouen ->
                    TumeKyouenModel(
                        tumeKyouen.stageNo,
                        tumeKyouen.size,
                        tumeKyouen.stage,
                        tumeKyouen.creator,
                        tumeKyouen.clearFlag,
                        Date(tumeKyouen.clearDate))
                }
            }
    }

    fun updateClearFlag(stageNo: Int, date: Date): Completable {
        val dao = appDatabase.tumeKyouenDao()
        return dao.findStage(stageNo)
            .flatMap { stage ->
                stage.clearFlag = TumeKyouenModel.CLEAR
                stage.clearDate = date.time
                dao.updateAll(stage)
                    .toMaybe<TumeKyouenModel>()
            }
            .ignoreElement()
    }

    fun updateSyncClearData(clearList: List<AddAllResponse.Stage>): Completable {
        return Completable.merge(
            clearList.map {
                updateClearFlag(it.stageNo, it.clearDate)
            }
        )
    }
}
