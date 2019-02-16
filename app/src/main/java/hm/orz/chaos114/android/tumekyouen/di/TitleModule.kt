package hm.orz.chaos114.android.tumekyouen.di

import android.content.Context
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleViewModelFactory
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.util.SoundManager

@Module
class TitleModule {
    @Provides
    fun provideTitleViewModelFactory(
            context: Context,
            tumeKyouenRepository: TumeKyouenRepository,
            soundManager: SoundManager
    ): TitleViewModelFactory {
        return TitleViewModelFactory(
                context,
                tumeKyouenRepository,
                soundManager
        )
    }
}
