package hm.orz.chaos114.android.tumekyouen.util

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.util.SparseIntArray

import javax.inject.Inject

import hm.orz.chaos114.android.tumekyouen.R

class SoundManager @Inject
constructor(private val preferenceUtil: PreferenceUtil, context: Context) {

    private val soundPool: SoundPool

    private val soundIds: SparseIntArray

    /**
     * Return flag of playing sound.
     *
     * @return flag of playing sound
     */
    var isPlayable: Boolean
        get() = preferenceUtil.getBoolean(PreferenceUtil.KEY_SOUND)
        private set(playable) = preferenceUtil.putBoolean(PreferenceUtil.KEY_SOUND, playable)

    init {

        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundIds = SparseIntArray()
        soundIds.put(R.raw.se_maoudamashii_se_finger01,
                soundPool.load(context, R.raw.se_maoudamashii_se_finger01, 1))
        soundIds.put(R.raw.se_maoudamashii_onepoint23,
                soundPool.load(context, R.raw.se_maoudamashii_onepoint23, 1))
    }

    /**
     * Toggle flag of playing sound.
     */
    fun togglePlayable() {
        val playable = isPlayable
        isPlayable = !playable
    }

    /**
     * 音を再生します。
     *
     * @param id 再生対象のID
     */
    fun play(id: Int) {
        if (!isPlayable) {
            return
        }

        val soundId = soundIds.get(id)
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}
