package hm.orz.chaos114.android.tumekyouen.usecase

import hm.orz.chaos114.android.tumekyouen.db.entities.TumeKyouen
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenV2Service
import hm.orz.chaos114.android.tumekyouen.network.models.Stage
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import io.reactivex.Single
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertDataTask @Inject constructor(
    private val tumeKyouenV2Service: TumeKyouenV2Service,
    private val tumeKyouenRepository: TumeKyouenRepository
) {
    var running = false
        private set

    fun run(currentMaxStageNo: Int, count: Int): Single<Int> {
        if (running) {
            return Single.error(ExclusiveException())
        }
        running = true
        return Single.create { emitter ->
            GlobalScope.launch {
                val response = tumeKyouenV2Service.getStages(currentMaxStageNo + 1, count * 10)
                if (response.isSuccessful) {
                    response.body()?.let {
                        insertData(it)
                        emitter.onSuccess(it.size)
                    }
                }
                running = false
            }
        }
    }

    private fun insertData(stages: List<Stage>) {
        val dbStages = stages.map {
            TumeKyouen(
                0,
                it.stageNo.toInt(),
                it.size.toInt(),
                it.stage,
                it.creator,
                0,
                0
            )
        }
        tumeKyouenRepository.insertStages(dbStages).subscribe()
    }
}

class ExclusiveException : RuntimeException()
