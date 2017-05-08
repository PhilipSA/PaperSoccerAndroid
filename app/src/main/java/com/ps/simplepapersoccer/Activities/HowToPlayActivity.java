package com.ps.simplepapersoccer.Activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ps.simplepapersoccer.R;

public class HowToPlayActivity extends AppCompatActivity {

    ImageView leftArrow;
    ImageView rightArrow;
    TextView viewPagerText;
    private int[] sliderTextId = new int[]{ R.string.tutorial1_text, R.string.tutorial2_text, R.string.tutorial3_text, R.string.tutorial4_text };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leftArrow = (ImageView)findViewById(R.id.left_nav);
        rightArrow = (ImageView)findViewById(R.id.right_nav);

        viewPagerText = (TextView) findViewById(R.id.viewpagerText);

        leftArrow.setVisibility(View.INVISIBLE);

        final ViewPager mViewPager = (ViewPager)findViewById(R.id.viewpager);
        TutorialPagerAdapter adapterView = new TutorialPagerAdapter(this, this);
        mViewPager.setAdapter(adapterView);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPagerText.setText(getString(sliderTextId[position]));
                if (position == 0)
                {
                    leftArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
                else if (mViewPager.getAdapter().getCount() - 1 == position){
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.INVISIBLE);
                }
                else {
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
