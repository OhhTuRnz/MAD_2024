package com.example.mad_2024_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import java.io.IOException
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SecondActivity : AppCompatActivity() {

    private val TAG = "LogoGPS2ndActivity"
    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location", Location::class.java)
        if (location != null) {
            latestLocation = location
            Log.i(
                TAG,
                "onCreate: Location[" + location.altitude + "][" + location.latitude + "][" + location.longitude + "]"
            )
        }

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
                    Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    // Handle nav_profile click (Profile)
                    Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

    }

    fun onNextButtonClick(view: View) {
        // This is the handler
        Toast.makeText(this, "Going to the third layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, ThirdActivity::class.java)
        startActivity(intent)
    }

    fun onPrevButtonClick(view: View) {
        // This is the handler
        Toast.makeText(this, "Going to the main layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun readFileContents(): List<List<String>> {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.map { it.split(";").map(String::trim) }.toList()
            }
        } catch (e: IOException) {
            listOf(listOf("Error reading file: ${e.message}"))
        }
    }

    private fun readFileLines(): List<String> {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().readLines()
        } catch (e: IOException) {
            listOf("Error reading file: ${e.message}")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goHome(view: View){
        // go to Main
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goMaps(view: View){
        // go to OpenStreetMaps
        if (::latestLocation.isInitialized) {
            Toast.makeText(this, "Going to OpenStreetMaps!", Toast.LENGTH_SHORT).show()
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

    private fun goSettings(view: View){
        // go to Settings
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }
}


