package hm.orz.chaos114.android.tumekyouen.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import hm.orz.chaos114.android.tumekyouen.modules.create.CreateActivity;
import hm.orz.chaos114.android.tumekyouen.modules.initial.InitialActivity;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    public abstract InitialActivity contributeInitialActivityInjector();

    @ContributesAndroidInjector
    public abstract TitleActivity contributeTitleActivityInjector();

    @ContributesAndroidInjector
    public abstract KyouenActivity contributeKyouenActivityInjector();

    @ContributesAndroidInjector
    public abstract CreateActivity contributeCreateActivityInjector();
}
