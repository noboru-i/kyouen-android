package hm.orz.chaos114.android.tumekyouen.modules.title

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TitleViewModel @Inject constructor(
        private val context: Context,
        private val tumeKyouenRepository: TumeKyouenRepository,
        private val soundManager: SoundManager
) : ViewModel() {

    private val stageCountModel = MutableLiveData<StageCountModel>()

    private val disposable: CompositeDisposable = CompositeDisposable()

    val displayStageCount: LiveData<String> = Transformations.map(stageCountModel) { stageCountModel ->
        context.getString(R.string.stage_count,
                stageCountModel.clearStageCount,
                stageCountModel.stageCount)
    }

    val soundResource: LiveData<Drawable> = Transformations.map(soundManager.isPlayable.toFlowable(BackpressureStrategy.BUFFER).toLiveData()) { isPlayable ->
        @DrawableRes val imageRes = if (isPlayable) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
        ContextCompat.getDrawable(context, imageRes)
    }

    fun refresh() {
        disposable.add(
                tumeKyouenRepository.selectStageCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { stageCountModel ->
                            this.stageCountModel.value = stageCountModel
                        }
        )
    }

    fun toggleSoundPlayable() {
        soundManager.togglePlayable()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
