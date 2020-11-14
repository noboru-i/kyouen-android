package hm.orz.chaos114.android.tumekyouen.modules.title

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenV2Service
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import javax.inject.Inject

class TitleViewModelFactory @Inject constructor(
    private val context: Context,
    private val loginUtil: LoginUtil,
    private val tumeKyouenV2Service: TumeKyouenV2Service,
    private val tumeKyouenRepository: TumeKyouenRepository,
    private val soundManager: SoundManager,
    private val insertDataTask: InsertDataTask
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TitleViewModel(
            context,
            loginUtil,
            tumeKyouenV2Service,
            tumeKyouenRepository,
            soundManager,
            insertDataTask
        ) as T
    }
}
