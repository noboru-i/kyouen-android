package hm.orz.chaos114.android.tumekyouen.usecase

import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import io.reactivex.Single
import javax.inject.Inject

class InsertDataTask @Inject constructor(
        private val tumeKyouenService: TumeKyouenService,
        private val tumeKyouenRepository: TumeKyouenRepository
) {
    var running = false
        private set
    fun run(startStageNo: Int, count: Int): Single<Int> {
        if (running) {
            return Single.error(ExclusiveException())
        }
        running = true
        return Single.create { emitter ->
            var stageNo = startStageNo
            for (i in 1..count) {
                val response = tumeKyouenService.getStage(stageNo).blockingGet()
                val responseString = response.body()?.string()
                if (responseString == null || "no_data".equals(responseString)) {
                    emitter.onSuccess(stageNo)
                    return@create
                }

                stageNo += insertData(responseString)
            }

            emitter.onSuccess(stageNo - startStageNo)
            running = false
        }
    }

    private fun insertData(responseString: String): Int {
        var count = 0
        responseString.split("\n").forEach { csvString ->
            tumeKyouenRepository.insertByCSV(csvString).blockingAwait()
            count++
        }
        return count
    }
}

class ExclusiveException(message: String? = null, cause: Throwable? = null) : RuntimeException()