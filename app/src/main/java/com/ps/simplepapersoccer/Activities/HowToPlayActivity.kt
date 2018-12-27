package com.ps.simplepapersoccer.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.ps.simplepapersoccer.R
import kotlinx.android.synthetic.main.activity_how_to_play.*

class HowToPlayActivity : AppCompatActivity() {
    private val sliderTextId = intArrayOf(R.string.tutorial1_text, R.string.tutorial2_text, R.string.tutorial3_text, R.string.tutorial4_text)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_play)

        left_nav?.visibility = View.INVISIBLE

        val mViewPager = findViewById<ViewPager>(R.id.viewpager)
        val adapterView = TutorialPagerAdapter(this)
        mViewPager.adapter = adapterView

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                viewpagerText?.text = getString(sliderTextId[position])
                when (position) {
                    0 -> {
                        left_nav?.visibility = View.INVISIBLE
                        right_nav?.visibility = View.VISIBLE
                    }
                    (mViewPager.adapter?.count ?: 0) - 1 -> {
                        left_nav?.visibility = View.VISIBLE
                        right_nav?.visibility = View.INVISIBLE
                    }
                    else -> {
                        left_nav?.visibility = View.VISIBLE
                        right_nav?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
