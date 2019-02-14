package hm.orz.chaos114.android.tumekyouen.util

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.util.SparseIntArray
import hm.orz.chaos114.android.tumekyouen.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
        val preferenceUtil: PreferenceUtil,
        context: Context
) {

    private val soundPool: SoundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)

    private val soundIds: SparseIntArray = SparseIntArray()

    var isPlayable: Boolean
        get() = preferenceUtil.getBoolean(PreferenceUtil.KEY_SOUND)
        private set(playable) = preferenceUtil.putBoolean(PreferenceUtil.KEY_SOUND, playable)

    init {
        soundIds.put(R.raw.se_maoudamashii_se_finger01,
                soundPool.load(context, R.raw.se_maoudamashii_se_finger01, 1))
        soundIds.put(R.raw.se_maoudamashii_onepoint23,
                soundPool.load(context, R.raw.se_maoudamashii_onepoint23, 1))
    }

    /**
     * Toggle flag of playing sound.
     */
    fun togglePlayable() {
        isPlayable = !isPlayable
    }

    fun play(id: Int) {
        if (!isPlayable) {
            return
        }

        val soundId = soundIds.get(id)
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}
