package hm.orz.chaos114.android.tumekyouen.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hm.orz.chaos114.android.tumekyouen.modules.create.CreateActivity
import hm.orz.chaos114.android.tumekyouen.modules.initial.InitialActivity
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeInitialActivityInjector(): InitialActivity

    @ContributesAndroidInjector
    abstract fun contributeTitleActivityInjector(): TitleActivity

    @ContributesAndroidInjector
    abstract fun contributeKyouenActivityInjector(): KyouenActivity

    @ContributesAndroidInjector
    abstract fun contributeCreateActivityInjector(): CreateActivity
}
