package hm.orz.chaos114.android.tumekyouen.repository

import com.google.gson.internal.bind.util.ISO8601Utils
import hm.orz.chaos114.android.tumekyouen.db.AppDatabase
import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.model.TumeKyouenModel
import hm.orz.chaos114.android.tumekyouen.network.models.ClearedStage
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.text.ParsePosition
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TumeKyouenRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun insertStages(stages: List<TumeKyouen>): Completable {
        return Completable.merge(stages.map {
            appDatabase.tumeKyouenDao().insertAll(it)
        })
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
                    Date(it.clearDate)
                )
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
                        Date(tumeKyouen.clearDate)
                    )
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

    fun updateSyncClearData(clearList: List<ClearedStage>): Completable {
        return Completable.merge(
            clearList.map {
                updateClearFlag(
                    it.stageNo.toInt(),
                    ISO8601Utils.parse(it.clearDate, ParsePosition(0))
                )
            }
        )
    }
}
