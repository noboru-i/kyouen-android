package hm.orz.chaos114.android.tumekyouen.modules.title

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import javax.inject.Inject

class TitleViewModelFactory @Inject constructor(
        private val context: Context,
        private val tumeKyouenRepository: TumeKyouenRepository,
        private val soundManager: SoundManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TitleViewModel(
                context,
                tumeKyouenRepository,
                soundManager
        ) as T
    }
}
