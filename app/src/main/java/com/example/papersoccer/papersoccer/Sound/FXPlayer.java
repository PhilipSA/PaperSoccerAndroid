package com.example.papersoccer.papersoccer.Sound;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import com.example.papersoccer.papersoccer.R;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.AUDIO_SERVICE;

public class FXPlayer
{
    static SoundPool soundPool;
    static Map<Integer, Integer> soundMap;
    static AudioManager amg;

    public static void InitSound(Activity activity) {

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

        amg = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public static void playSound(int sound) {

        soundPool.play(soundMap.get(sound), 1, 1, 1, 0, 1f);
    }

    public final void cleanUpIfEnd() {
        soundMap = null;
        soundPool.release();
        soundPool = null;
    }
}
