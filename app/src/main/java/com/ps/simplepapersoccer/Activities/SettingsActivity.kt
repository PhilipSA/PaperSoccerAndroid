package com.ps.simplepapersoccer.activities

import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

import com.ps.simplepapersoccer.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, MyPreferenceFragment()).commit()
    }

    class MyPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}
