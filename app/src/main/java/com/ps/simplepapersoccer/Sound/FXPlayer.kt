package com.ps.simplepapersoccer.Sound

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.SparseIntArray

import com.ps.simplepapersoccer.R

import java.util.HashMap

class FXPlayer(activity: Activity) {
    private var soundPool: SoundPool
    private var soundMap: SparseIntArray
    private val audioManager: AudioManager

    init {

        val maxStreams = 1
        val mContext = activity.applicationContext
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build()
        } else {
            SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        soundMap = SparseIntArray()
        // fill your sounds
        soundMap[R.raw.bounce] = soundPool.load(mContext, R.raw.bounce, 1)
        soundMap.put(R.raw.soccerkick, soundPool.load(mContext, R.raw.soccerkick, 2))
        soundMap.put(R.raw.failure, soundPool.load(mContext, R.raw.failure, 1))
        soundMap.put(R.raw.goodresult, soundPool.load(mContext, R.raw.goodresult, 1))

        audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun playSound(sound: Int) {
        soundPool.play(soundMap[sound], 1f, 1f, 1, 0, 1f)
    }

    fun cleanUpIfEnd() {
        soundMap = null
        soundPool.release()
        soundPool = null
    }
}
