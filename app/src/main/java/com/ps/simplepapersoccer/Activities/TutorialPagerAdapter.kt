package com.ps.simplepapersoccer.Activities

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView

import com.ps.simplepapersoccer.R

class TutorialPagerAdapter internal constructor(internal var mContext: Context, internal var activity: Activity) : PagerAdapter() {

    override fun getCount(): Int {
        return sliderImagesId.size
    }

    private val sliderImagesId = intArrayOf(R.drawable.tutorial1, R.drawable.tutorial2, R.drawable.tutorial3, R.drawable.tutorial4)

    override fun isViewFromObject(v: View, obj: Any): Boolean {
        return v === obj
    }

    override fun instantiateItem(container: ViewGroup, i: Int): Any {
        val imageView = ImageView(mContext)

        imageView.setImageDrawable(ContextCompat.getDrawable(mContext, sliderImagesId[i]))

        (container as ViewPager).addView(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, i: Int, obj: Any) {
        (container as ViewPager).removeView(obj as ImageView)
    }
}