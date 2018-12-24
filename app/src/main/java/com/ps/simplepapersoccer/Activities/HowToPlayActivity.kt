package com.ps.simplepapersoccer.Activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.ps.simplepapersoccer.R

class HowToPlayActivity : AppCompatActivity() {

    internal var leftArrow: ImageView? = null
    internal var rightArrow: ImageView? = null
    internal var viewPagerText: TextView? = null
    private val sliderTextId = intArrayOf(R.string.tutorial1_text, R.string.tutorial2_text, R.string.tutorial3_text, R.string.tutorial4_text)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_play)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        leftArrow = findViewById<ImageView>(R.id.left_nav)
        rightArrow = findViewById<ImageView>(R.id.right_nav)

        viewPagerText = findViewById<TextView>(R.id.viewpagerText)

        leftArrow?.visibility = View.INVISIBLE

        val mViewPager = findViewById<ViewPager>(R.id.viewpager)
        val adapterView = TutorialPagerAdapter(this, this)
        mViewPager.adapter = adapterView

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewPagerText?.text = getString(sliderTextId[position])
                if (position == 0) {
                    leftArrow?.visibility = View.INVISIBLE
                    rightArrow?.visibility = View.VISIBLE
                } else if (mViewPager.adapter.count - 1 == position) {
                    leftArrow?.visibility = View.VISIBLE
                    rightArrow?.visibility = View.INVISIBLE
                } else {
                    leftArrow?.visibility = View.VISIBLE
                    rightArrow?.visibility = View.VISIBLE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
