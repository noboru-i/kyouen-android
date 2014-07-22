package hm.orz.chaos114.android.tumekyouen;

import android.app.Activity;
import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import hm.orz.chaos114.android.tumekyouen.shadows.ShadowAdView;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowAdView.class})
public class TitleActivityTest {

    @Test
    public void clickButton() throws Exception {
        Activity activity = Robolectric.buildActivity(TitleActivity_.class).create().get();

        Button startButton = (Button) activity.findViewById(R.id.start_button);
        startButton.performClick();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("start"));
    }
}
