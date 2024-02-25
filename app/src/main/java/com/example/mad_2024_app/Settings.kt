package com.example.mad_2024_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.mad_2024_app.ui.theme.MAD_2024_AppTheme
class Settings : ComponentActivity(){

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Retrieve and display user ID
        displayUserId(sharedPreferences)

        // Set switch state based on saved preference
        switchDarkMode.isChecked = sharedPreferences.getBoolean("darkModeEnabled", false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPreferences.edit()) {
                putBoolean("darkModeEnabled", isChecked)
                apply()
            }
        }
    }

    private fun displayUserId(sharedPreferences: SharedPreferences) {
        val userId = sharedPreferences.getString("userId", "Not Set")
        val tvUserId: TextView = findViewById(R.id.tvUserId)
        tvUserId.text = "User ID: $userId"
    }
    fun onPrevButtonClick(view: View){
        // This is the handler
        Toast.makeText(this, "Going to the main layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}