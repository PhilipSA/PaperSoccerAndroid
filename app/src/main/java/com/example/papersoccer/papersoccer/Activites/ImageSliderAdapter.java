package com.example.papersoccer.papersoccer.Activites;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.papersoccer.papersoccer.R;

/**
 * Created by Admin on 2017-04-26.
 */

public class ImageSliderAdapter extends PagerAdapter {
    Context mContext;

    ImageSliderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return sliderImagesId.length;
    }

    private int[] sliderImagesId = new int[]{ R.drawable.tutorial1, R.drawable.tutorial2 };

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == ((ImageView) obj);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int i) {
        ImageView mImageView = new ImageView(mContext);

        //mImageView.setImageResource(sliderImagesId[i]);
        Glide.with(mContext).load(sliderImagesId[i]).into(new GlideDrawableImageViewTarget(mImageView));
        //Glide.with(this.mContext).load(sliderImagesId[i]).into(mImageView);

        ((ViewPager) container).addView(mImageView, 0);
        return mImageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int i, Object obj) {
        ((ViewPager) container).removeView((ImageView) obj);
    }
}
