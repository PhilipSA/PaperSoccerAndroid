package com.ps.simplepapersoccer.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.view.Window;

import com.ps.simplepapersoccer.Enums.GameModeEnum;
import com.ps.simplepapersoccer.R;
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
		final Intent intent = new Intent(this, GameActivity.class);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.choose_game_mode));
		builder.setItems(new CharSequence[]
						{getString(R.string.single_player_mode), getString(R.string.local_multiplayer_mode), getString(R.string.ai_vs_ai_mode)},
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						switch (which) {
							case 0:
								intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.PLAYER_VS_AI);
								break;
							case 1:
								intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.MULTIPLAYER_MODE);
								break;
							case 2:
								intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.AI_VS_AI);
								break;
						}
						startActivity(intent);
					}
				});
		builder.create().show();
	}

	public void SettingsClick(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void HowToPlayClick(View view)
	{
		Intent intent = new Intent(this, HowToPlayActivity.class);
		startActivity(intent);
	}
	
	public void ExitClick(View view)
	{
		finish();
		System.exit(0);
	}
}
