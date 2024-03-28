package com.example.mad_2024_app.Activities

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.view_models.UserViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var editName: EditText

    private lateinit var userRepo : UserRepository
    private lateinit var userViewModel: UserViewModel

    private val TAG = "LogoGPSProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_profile)

        val appContext = application as App

        initializeViewModels(appContext)

        editName = findViewById(R.id.edit_name) // Initialize after setContentView

        val profileImage: ImageView = findViewById(R.id.profile_image)
        val btnSave: Button = findViewById(R.id.btn_save)
        val btnBack: Button = findViewById(R.id.btn_back)

        loadProfileData()

        val rootView = findViewById<View>(android.R.id.content)

        btnSave.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            saveProfileData()
            goHome(rootView)
        }

        btnBack.setOnClickListener {
            goHome(rootView)
        }

        //migrateSharedPreferences()
    }

    private fun initializeViewModels(appContext: Context){
        userRepo = DbUtils.getUserRepository(appContext)
        val userFactory = ViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]
    }

    private fun migrateSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
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
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("username", "")
        Log.d(TAG, "loadingData: retrieved username $name")
        editName.setHint("Change your name here $name")
    }

    private fun saveProfileData() {
        val name = editName.text.toString()
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val uuid = sharedPreferences.getString("userId", null)
        Log.d(TAG, "Saving user in database with uuid: $uuid and username: $name")
        lifecycleScope.launch {
            if (uuid != null) {
                userViewModel.getUserByUUIDPreCollect(uuid).collect { user ->
                    // Collect the user data from the Flow
                    user?.let {
                        userViewModel.upsertUser(user.copy(username = name))

                        with(sharedPreferences.edit()) {
                            putString("username", name)
                            apply()
                        }
                        Toast.makeText(this@ProfileActivity, "Profile saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun goHome(view: View){
        // go to Main
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
