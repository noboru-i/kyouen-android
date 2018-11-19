package hm.orz.chaos114.android.tumekyouen.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

import javax.inject.Inject;

import hm.orz.chaos114.android.tumekyouen.R;

public class SoundManager {

    private final PreferenceUtil preferenceUtil;

    private final SoundPool soundPool;

    private final SparseIntArray soundIds;

    @Inject
    public SoundManager(PreferenceUtil preferenceUtil, Context context) {
        this.preferenceUtil = preferenceUtil;

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundIds = new SparseIntArray();
        soundIds.put(R.raw.se_maoudamashii_se_finger01,
                soundPool.load(context, R.raw.se_maoudamashii_se_finger01, 1));
        soundIds.put(R.raw.se_maoudamashii_onepoint23,
                soundPool.load(context, R.raw.se_maoudamashii_onepoint23, 1));
    }

    /**
     * Return flag of playing sound.
     *
     * @return flag of playing sound
     */
    public boolean isPlayable() {
        return preferenceUtil.getBoolean(PreferenceUtil.KEY_SOUND);
    }

    /**
     * Toggle flag of playing sound.
     */
    public void togglePlayable() {
        boolean playable = isPlayable();
        setPlayable(!playable);
    }

    /**
     * 音を再生します。
     *
     * @param id 再生対象のID
     */
    public void play(int id) {
        if (!isPlayable()) {
            return;
        }

        int soundId = soundIds.get(id);
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    private void setPlayable(boolean playable) {
        preferenceUtil.putBoolean(PreferenceUtil.KEY_SOUND, playable);
    }
}
