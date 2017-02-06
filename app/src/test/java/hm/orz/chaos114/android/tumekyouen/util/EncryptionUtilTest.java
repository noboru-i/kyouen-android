package hm.orz.chaos114.android.tumekyouen.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class EncryptionUtilTest {

    EncryptionUtil util;

    @Before
    public void setup() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        util = new EncryptionUtil(new PreferenceUtil(sp));
    }

    @Test
    public void encrypt_decrypt_shouldBackOriginalText() {
        String testString = "test message";
        String encrypted = util.encrypt(testString);
        assertThat(testString, is(not(encrypted)));
        assertThat(testString, is(util.decrypt(encrypted)));
    }

    @Test
    public void encrypt_shouldReturnNull() {
        assertNull(util.encrypt(null));
    }

    @Test
    public void decrypt_shouldReturnNull() {
        assertNull(util.decrypt(null));
    }
}
