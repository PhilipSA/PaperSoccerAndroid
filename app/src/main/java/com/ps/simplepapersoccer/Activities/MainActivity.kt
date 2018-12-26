package com.ps.simplepapersoccer.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdView
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.R

class MainActivity : AppCompatActivity() {

    internal var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

/*        mAdView = findViewById(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)*/
    }

    fun SingleplayerClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_game_mode))
        builder.setItems(arrayOf<CharSequence>(getString(R.string.single_player_mode), getString(R.string.local_multiplayer_mode), getString(R.string.ai_vs_ai_mode))
        ) { dialog, which ->
            when (which) {
                0 -> intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.PLAYER_VS_AI)
                1 -> intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.MULTIPLAYER_MODE)
                2 -> intent.putExtra("MULTIPLAYER_MODE", GameModeEnum.AI_VS_AI)
            }
            startActivity(intent)
        }
        builder.create().show()
    }

    fun SettingsClick(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun HowToPlayClick(view: View) {
        val intent = Intent(this, HowToPlayActivity::class.java)
        startActivity(intent)
    }

    fun ExitClick(view: View) {
        finish()
        System.exit(0)
    }
}
