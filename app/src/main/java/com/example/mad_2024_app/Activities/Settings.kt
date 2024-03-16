package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.example.mad_2024_app.view_models.UserViewModel

class Settings : ComponentActivity(){

    private lateinit var viewModel: UserViewModel
    private lateinit var userRepo: UserRepository

    private val TAG : String = "SettingsActivity"
    private val tvUserId: TextView by lazy { findViewById(R.id.tvUserId) }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_settings)

        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        // Retrieve and display user ID
        //displayUserId(sharedPreferences)

        // Initialize ViewModel with ViewModelFactory
        val appContext = application as App
        userRepo = DbUtils.getUserRepository(appContext)
        val factory = ViewModelFactory(userRepo)
        viewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        fetchAndDisplayUserData(sharedPreferences)

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

    private fun fetchAndDisplayUserData(sharedPreferences: SharedPreferences) {
        val userUUID = sharedPreferences.getString("userId", null)
        userUUID?.let { uuid ->
            viewModel.getUserByUUID(uuid) // This is non-suspending and returns LiveData
        }

        // Observe the LiveData from ViewModel
        viewModel.user.observe(this) { user ->
            user?.let {
                // Update TextView with user UUID
                tvUserId.text = "${user.uuid}"
            } ?: run {
                // Handle case where user is null
                Log.d(TAG, "User missing or not found")
                // Optionally, update TextView to indicate user not found
                tvUserId.text = "$userUUID"
            }
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