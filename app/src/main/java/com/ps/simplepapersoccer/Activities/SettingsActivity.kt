package com.ps.simplepapersoccer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity

import com.ps.simplepapersoccer.R
import com.ps.simplepapersoccer.gameobjects.player.AIPlayer
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerTypes = AIPlayer.allAi.plus("Player")

        settings_player1_spinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, playerTypes)
        settings_player2_spinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, playerTypes)

        settings_play.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra(GameActivity.ARG_PLAYER1, settings_player1_spinner.selectedItem as String)
                putExtra(GameActivity.ARG_PLAYER2, settings_player2_spinner.selectedItem as String)
                putExtra(GameActivity.ARG_GRID_SIZE_X, settings_grid_size_x.text.toString().toInt())
                putExtra(GameActivity.ARG_GRID_SIZE_Y, settings_grid_size_y.text.toString().toInt())
            }
            startActivity(intent)
        }
    }
}
