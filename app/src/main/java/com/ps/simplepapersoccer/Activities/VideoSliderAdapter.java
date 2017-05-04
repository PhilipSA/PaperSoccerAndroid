package com.ps.simplepapersoccer.Activities;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.ps.simplepapersoccer.R;

public class VideoSliderAdapter extends PagerAdapter {
    Context mContext;
    Activity activity;

    VideoSliderAdapter(Context context, Activity activity) {
        this.mContext = context;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return sliderImagesId.length;
    }

    private int[] sliderImagesId = new int[]{ R.raw.tutorial1, R.raw.tutorial2 };

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == ((VideoView) obj);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int i) {
        VideoView videoView = new VideoView(mContext);

        String videoName = "tutorial1";

        int id = mContext.getResources().getIdentifier(videoName, "raw", activity.getPackageName());

        String uri = "android.resource://" + mContext.getPackageName() + "/" + sliderImagesId[i];

        videoView.setVideoURI(Uri.parse(uri));
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        ((ViewPager) container).addView(videoView, 0);
        return videoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int i, Object obj) {
        ((ViewPager) container).removeView((ImageView) obj);
    }
}
