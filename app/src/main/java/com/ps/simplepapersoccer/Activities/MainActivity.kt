package com.ps.simplepapersoccer.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.ps.simplepapersoccer.enums.GameModeEnum
import com.ps.simplepapersoccer.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
    }

    fun singleplayerClick(view: View) {
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

    fun settingsClick(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun howToPlayClick(view: View) {
        val intent = Intent(this, HowToPlayActivity::class.java)
        startActivity(intent)
    }

    fun exitClick(view: View) {
        finish()
        System.exit(0)
    }
}
