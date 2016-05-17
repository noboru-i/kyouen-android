package hm.orz.chaos114.android.tumekyouen.di;

import javax.inject.Singleton;

import dagger.Component;
import hm.orz.chaos114.android.tumekyouen.modules.initial.InitialActivity;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.TumeKyouenFragment;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivityViewModel;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(InitialActivity activity);

    void inject(TitleActivity activity);

    void inject(KyouenActivity activity);

    void inject(TumeKyouenFragment fragment);

    void inject(TitleActivityViewModel titleActivityViewModel);
}
