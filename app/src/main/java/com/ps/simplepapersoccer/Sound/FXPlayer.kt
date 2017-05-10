package com.ps.simplepapersoccer.Sound

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

import com.ps.simplepapersoccer.R

import java.util.HashMap

class FXPlayer(activity: Activity) {
    private var soundPool: SoundPool? = null
    private var soundMap: MutableMap<Int, Int>? = null
    private val audioManager: AudioManager

    init {

        val maxStreams = 1
        val mContext = activity.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build()
        } else {
            soundPool = SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        }

        soundMap = HashMap<Int, Int>()
        // fill your sounds
        soundMap!!.put(R.raw.bounce, soundPool!!.load(mContext, R.raw.bounce, 1))
        soundMap!!.put(R.raw.soccerkick, soundPool!!.load(mContext, R.raw.soccerkick, 2))
        soundMap!!.put(R.raw.failure, soundPool!!.load(mContext, R.raw.failure, 1))
        soundMap!!.put(R.raw.goodresult, soundPool!!.load(mContext, R.raw.goodresult, 1))

        audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun playSound(sound: Int) {

        if (soundPool != null) soundPool!!.play(soundMap!![sound] as Int, 1f, 1f, 1, 0, 1f)
    }

    fun cleanUpIfEnd() {
        soundMap = null
        soundPool!!.release()
        soundPool = null
    }
}
