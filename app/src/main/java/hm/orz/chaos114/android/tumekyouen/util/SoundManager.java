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

    /**
     * プライベートコンストラクタ。
     */
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
     * 音再生フラグを返します。
     *
     * @return 音再生フラグ
     */
    public boolean isPlayable() {
        return preferenceUtil.getBoolean(PreferenceUtil.KEY_SOUND);
    }

    /**
     * 音再生フラグを設定します。
     *
     * @param playable 音再生フラグ
     */
    public void setPlayable(boolean playable) {
        preferenceUtil.putBoolean(PreferenceUtil.KEY_SOUND, playable);
    }

    /**
     * 音再生フラグを変更します。
     *
     * @return 変更後の音再生フラグ
     */
    public boolean switchPlayable() {
        boolean playable = isPlayable();
        setPlayable(!playable);
        return !playable;
    }

    /**
     * 音を再生します。
     *
     * @param id 再生対象のID
     */
    public void play(int id) {
        play(id, false);
    }

    /**
     * 音を再生します。
     *
     * @param id    再生対象のID
     * @param force trueの場合、強制的に再生する
     */
    public void play(int id, boolean force) {
        boolean playable = true;
        if (!force) {
            if (!isPlayable()) {
                playable = false;
            }
        }
        if (!playable) {
            return;
        }

        int soundId = soundIds.get(id);
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }
}
