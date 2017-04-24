package com.example.papersoccer.papersoccer.Activites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.papersoccer.papersoccer.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity {

	AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
	}
	
	public void SingleplayerClick(View view)
	{
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}

	public void LocalMultiplayerClick(View view)
	{
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra("MULTIPLAYER_MODE", true);
		startActivity(intent);
	}

	public void SettingsClick(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void ExitClick(View view)
	{
		finish();
	}
}
