package com.example.mad_2024_app.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.view_models.AddressViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class FavoriteDonutsActivity : AppCompatActivity() {
    private val TAG = "LogoGPSFavDonutActivity"
    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_favorite_donuts)

        toggleDrawer()

    }

    private fun applyTheme(sharedPreferences: SharedPreferences) {
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun toggleDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_drawer)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // Handle nav_home click (Home)
                    val rootView = findViewById<View>(android.R.id.content)
                    goHome(rootView)
                    true
                }

                R.id.nav_maps -> {
                    // Handle nav_maps click (OpenStreetMaps)
                    Toast.makeText(applicationContext, "OpenStreetMaps", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goMaps(rootView)
                    true
                }

                R.id.nav_settings -> {
                    // Handle nav_settings click (Settings)
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goSettings(rootView)
                    true
                }

                R.id.nav_login -> {
                    // Handle nav_login click (Login)
                    val rootView = findViewById<View>(android.R.id.content)
                    goLogin(rootView)
                    true
                }

                R.id.nav_profile -> {
                    // Handle nav_profile click (Profile)
                    Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goProfile(rootView)
                    true
                }

                R.id.nav_logout -> {
                    logoutUser()
                    true
                }

                else -> false
            }
        }
    }

    fun onPrevButtonClick(view: View) {
        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goHome(view: View) {
        // go to Main
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    fun goMaps(view: View) {
        // go to OpenStreetMaps
        if (::latestLocation.isInitialized) {
            val intent = Intent(this, OpenStreetMap::class.java).apply {
                putExtra("locationBundle", Bundle().apply {
                    putParcelable("location", latestLocation)
                })
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    fun goSettings(view: View) {
        // go to Settings
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun goProfile(view: View) {
        // go to Settings
        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
    }

    private fun goLogin(view: View) {
        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, show a toast message and do not navigate
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show()
        } else {
            // User is not logged in, navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Optionally, you can remove this toast to avoid redundancy
            Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is logged in, proceed with logout
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Redirect to login screen or another appropriate activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optionally, close the current activity
        } else {
            // User is not logged in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}