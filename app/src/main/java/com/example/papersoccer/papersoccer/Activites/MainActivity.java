package com.example.papersoccer.papersoccer.Activites;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.papersoccer.papersoccer.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
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
