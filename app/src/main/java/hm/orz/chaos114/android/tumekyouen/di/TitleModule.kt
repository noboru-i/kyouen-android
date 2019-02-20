package hm.orz.chaos114.android.tumekyouen.di

import android.content.Context
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleViewModelFactory
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager

@Module
class TitleModule {
    @Provides
    fun provideTitleViewModelFactory(
        context: Context,
        loginUtil: LoginUtil,
        tumeKyouenService: TumeKyouenService,
        tumeKyouenRepository: TumeKyouenRepository,
        soundManager: SoundManager,
        insertDataTask: InsertDataTask
    ): TitleViewModelFactory {
        return TitleViewModelFactory(
            context,
            loginUtil,
            tumeKyouenService,
            tumeKyouenRepository,
            soundManager,
            insertDataTask
        )
    }
}
