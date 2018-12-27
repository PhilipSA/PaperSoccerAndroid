package com.ps.simplepapersoccer.activities

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.ps.simplepapersoccer.R

class TutorialPagerAdapter internal constructor(private var context: Context) : PagerAdapter() {

    override fun getCount(): Int {
        return sliderImagesId.size
    }

    private val sliderImagesId = intArrayOf(R.drawable.tutorial1, R.drawable.tutorial2, R.drawable.tutorial3, R.drawable.tutorial4)

    override fun isViewFromObject(v: View, obj: Any): Boolean {
        return v == obj
    }

    override fun instantiateItem(container: ViewGroup, i: Int): Any {
        val imageView = ImageView(context)

        imageView.setImageDrawable(ContextCompat.getDrawable(context, sliderImagesId[i]))

        (container as ViewPager).addView(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, i: Int, obj: Any) {
        (container as ViewPager).removeView(obj as ImageView)
    }
}
