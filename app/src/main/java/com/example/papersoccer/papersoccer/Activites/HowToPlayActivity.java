package com.example.papersoccer.papersoccer.Activites;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import com.example.papersoccer.papersoccer.R;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPageAndroid);
        ImageSliderAdapter adapterView = new ImageSliderAdapter(this);
        mViewPager.setAdapter(adapterView);
    }
}
