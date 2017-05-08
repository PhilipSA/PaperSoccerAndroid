package com.ps.simplepapersoccer.Sound;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.ps.simplepapersoccer.R;

import java.util.HashMap;
import java.util.Map;

public class FXPlayer
{
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;
    private AudioManager audioManager;

    public FXPlayer(Activity activity) {

        int maxStreams = 1;
        Context mContext = activity.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        soundMap = new HashMap<>();
        // fill your sounds
        soundMap.put(R.raw.bounce, soundPool.load(mContext, R.raw.bounce, 1));
        soundMap.put(R.raw.soccerkick, soundPool.load(mContext, R.raw.soccerkick, 2));
        soundMap.put(R.raw.failure, soundPool.load(mContext, R.raw.failure, 1));
        soundMap.put(R.raw.goodresult, soundPool.load(mContext, R.raw.goodresult, 1));

        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void playSound(int sound) {

        if (soundPool != null) soundPool.play(soundMap.get(sound), 1, 1, 1, 0, 1f);
    }

    public final void cleanUpIfEnd() {
        soundMap = null;
        soundPool.release();
        soundPool = null;
    }
}
