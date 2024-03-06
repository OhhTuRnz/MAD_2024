package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import android.widget.Switch
import android.widget.TextView
import com.example.mad_2024_app.R

class Settings : ComponentActivity(){

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_settings)

        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        // Retrieve and display user ID
        displayUserId(sharedPreferences)

        // Set switch state based on saved preference
        switchDarkMode.isChecked = sharedPreferences.getBoolean("darkModeEnabled", false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("darkModeEnabled", isChecked)
                apply()
            }
            if(isChecked){
                setTheme(R.style.AppTheme_Dark)
            } else {
                setTheme(R.style.AppTheme_Light)
            }
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

    private fun applyTheme(sharedPreferences: SharedPreferences){
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun displayUserId(sharedPreferences: SharedPreferences) {
        val userId = sharedPreferences.getString("userId", "Not Set")
        val tvUserId: TextView = findViewById(R.id.tvUserId)
        tvUserId.text = "User ID: $userId"
    }
    fun onPrevButtonClick(view: View){
            // go to another activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
    }
}