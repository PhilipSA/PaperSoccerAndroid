package com.example.papersoccer.papersoccer.Sound;

import android.app.Activity;
import android.media.MediaPlayer;

/**
 * Created by Admin on 2017-04-24.
 */

public class FXPlayer
{
    public MediaPlayer mediaPlayer;
    int soundId;
    Activity activity;

    public FXPlayer(Activity activity, int soundId)
    {
        this.activity = activity;
        this.soundId = soundId;
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void PlaySoundEffect()
    {
        stop();

        mediaPlayer = MediaPlayer.create(activity, soundId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });

        mediaPlayer.start();
    }
}
