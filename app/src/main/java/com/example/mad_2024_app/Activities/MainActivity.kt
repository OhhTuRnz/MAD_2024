package com.example.mad_2024_app.Activities

import Utils
import Utils.Companion.saveCoordinatesToFile
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import java.util.UUID
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.mad_2024_app.R
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var latestLocation: Location
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var toggle: ActionBarDrawerToggle

    private val locationPermissionCode = 2
    private val TAG = "LogoGPSMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_main)

        toggleDrawer()

        createUUID(sharedPreferences)

        val backgroundImageView: ImageView = findViewById(R.id.backgroundImageView)
        val gifUrl = "https://art.ngfiles.com/images/2478000/2478561_slavetomyself_spinning-donut-gif.gif?f1650761565"
        Glide.with(this).load(gifUrl).into(backgroundImageView)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupPermissionLauncher()
        checkPermissionsAndStartLocationUpdates()

        Log.d(TAG, "onCreate: Main activity is being created")
    }

    override fun onDestroy() {
        super.onDestroy()

        Toast.makeText(this, "prueba", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "OnDestroy: MAIN DESTROYED")
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        // Reset isFirstOpen to true when the app is closed or sent to the background
        with(sharedPreferences.edit()) {
            putBoolean("isFirstOpen", true)
            apply()
        }
    }

    private fun createUUID(sharedPreferences : SharedPreferences){
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            // Generate a unique user ID. Here we are using a random UUID.
            val newUserId = UUID.randomUUID().toString()

            with(sharedPreferences.edit()) {
                putString("userId", newUserId)
                apply()
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
    fun onNextButtonClick(view: View) {
        if (::latestLocation.isInitialized) {
            val intent = Intent(this, SecondActivity::class.java).apply {
                putExtra("locationBundle", Bundle().apply {
                    putParcelable("location", latestLocation)
                })
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun toggleDrawer(){
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
                    val rootView = findViewById<View>(android.R.id.content)
                    goProfile(rootView)
                    true
                }
                else -> false
            }
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        // Get last known location immediately
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        }
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Check for location permissions
            val granted = permissions.entries.all { it.value }
            if (granted) {
                // PERMISSION GRANTED
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f,
                        this
                    )
                }
            } else {
                // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
                // whichever happens first
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        latestLocation = location
        runOnUiThread {
            val textView: TextView = findViewById(R.id.mainTextView)
            textView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        }
        saveCoordinatesToFile(location.latitude, location.longitude, filesDir)
        Utils.writeLocationToCSV(this, location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun Greeting(name: String): String {
        return "Hello ${name}!"
    }

    private fun goHome(view: View){
        // go to Main
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goMaps(view: View){
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

    fun goSettings(view: View){
        // go to Settings
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

    private fun goProfile(view: View){
        // go to Settings
        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
    }
}
