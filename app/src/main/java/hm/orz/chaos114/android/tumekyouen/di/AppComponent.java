package hm.orz.chaos114.android.tumekyouen.di;

import javax.inject.Singleton;

import dagger.Component;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.KyouenActivity;
import hm.orz.chaos114.android.tumekyouen.modules.kyouen.TumeKyouenFragment;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivity;
import hm.orz.chaos114.android.tumekyouen.modules.title.TitleActivityViewModel;
import hm.orz.chaos114.android.tumekyouen.util.EncryptionUtil;
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil;
import hm.orz.chaos114.android.tumekyouen.util.PreferenceUtil;
import hm.orz.chaos114.android.tumekyouen.util.SoundManager;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(TitleActivity activity);

    void inject(KyouenActivity activity);

    void inject(TumeKyouenFragment fragment);

    void inject(LoginUtil loginUtil);

    void inject(EncryptionUtil encryptionUtil);

    void inject(PreferenceUtil preferenceUtil);

    void inject(SoundManager soundManager);

    void inject(TitleActivityViewModel titleActivityViewModel);
}
