package com.example.papersoccer.papersoccer.Activites;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.papersoccer.papersoccer.R;

public class HowToPlayActivity extends AppCompatActivity {

    ImageView leftArrow;
    ImageView rightArrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leftArrow = (ImageView)findViewById(R.id.left_nav);
        rightArrow = (ImageView)findViewById(R.id.right_nav);

        leftArrow.setVisibility(View.INVISIBLE);

        ViewPager mViewPager = (ViewPager)findViewById(R.id.viewpager);
        VideoSliderAdapter adapterView = new VideoSliderAdapter(this, this);
        mViewPager.setAdapter(adapterView);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                {
                    leftArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
                else {
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.INVISIBLE);
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
