package com.ps.simplepapersoccer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.ps.simplepapersoccer.R
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
    }

    fun singleplayerClick(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun howToPlayClick(view: View) {
        //val intent = Intent(this, HowToPlayActivity::class.java)
        val intent = Intent(this, NeuralNetworkVisualsActivity::class.java)
        startActivity(intent)
    }

    fun exitClick(view: View) {
        finish()
        exitProcess(0)
    }
}
