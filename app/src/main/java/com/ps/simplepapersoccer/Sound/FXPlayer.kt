package com.ps.simplepapersoccer.sound

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.SparseIntArray

import com.ps.simplepapersoccer.R

class FXPlayer(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var soundMap: HashMap<Int, Int>
    private val audioManager: AudioManager

    init {

        val maxStreams = 1
        val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build()
        } else {
            SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        soundMap = HashMap()
        // fill your sounds
        soundMap[R.raw.bounce] = soundPool.load(context, R.raw.bounce, 1)
        soundMap[R.raw.soccerkick] = soundPool.load(context, R.raw.soccerkick, 2)
        soundMap[R.raw.failure] = soundPool.load(context, R.raw.failure, 1)
        soundMap[R.raw.goodresult] = soundPool.load(context, R.raw.goodresult, 1)

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        this.soundPool = soundPool
    }

    fun playSound(sound: Int) {
        soundPool?.play(soundMap[sound]!!, 1f, 1f, 1, 0, 1f)
    }

    fun cleanUpIfEnd() {
        soundPool?.release()
        soundPool = null
    }
}
