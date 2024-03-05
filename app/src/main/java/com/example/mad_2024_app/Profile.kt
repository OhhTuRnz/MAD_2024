package com.example.mad_2024_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mad_2024_app.R
import com.google.android.material.navigation.NavigationView

class Profile : AppCompatActivity() {

    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var editName: EditText

    private val TAG = "LogoGPSProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_profile)

        editName = findViewById(R.id.edit_name) // Initialize after setContentView
        val profileImage: ImageView = findViewById(R.id.profile_image)
        val btnSave: Button = findViewById(R.id.btn_save)
        val btnBack: Button = findViewById(R.id.btn_back)

        loadProfileData()

        val rootView = findViewById<View>(android.R.id.content)

        btnSave.setOnClickListener {
            saveProfileData()
            goHome(rootView)
        }

        btnBack.setOnClickListener {
            goHome(rootView)
        }

        migrateSharedPreferences()
    }

    private fun migrateSharedPreferences() {
        val sharedPreferences = getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
        val oldName = sharedPreferences.getString("name", null)

        oldName?.let {
            with(sharedPreferences.edit()) {
                putString("username", it)
                remove("name")
                apply()
            }
        }
    }
    private fun applyTheme(sharedPreferences: SharedPreferences) {
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("username", "")
        Log.d(TAG, "loadingData: retrieved username $name")
        editName.setHint("Change your name here $name")
    }

    private fun saveProfileData() {
        val name = editName.text.toString()
        val sharedPreferences = getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("username", name)
            apply()
        }
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
    }

    private fun goHome(view: View){
        // go to Main
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
