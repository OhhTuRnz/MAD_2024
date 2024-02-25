package com.example.mad_2024_app

import Utils
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import java.util.UUID
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.io.File

class MainActivity : ComponentActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var latestLocation: Location
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val locationPermissionCode = 2
    private val TAG = "LogoGPSMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstOpen = sharedPreferences.getBoolean("isFirstOpen", true)
        val userId = sharedPreferences.getString("userId", null)

        if (userId == null) {
            // Generate a unique user ID. Here we are using a random UUID.
            val newUserId = UUID.randomUUID().toString()
            askForUserIdentifier()

            with(sharedPreferences.edit()) {
                putString("userId", newUserId)
                apply()
            }
        }
        else{
            Toast.makeText(this, "User ID: $userId", Toast.LENGTH_LONG).show()
        }

        if (isFirstOpen) {
            Toast.makeText(this, Greeting(name = "User"), Toast.LENGTH_SHORT).show()
            // Set isFirstOpen to false
            sharedPreferences.edit().putBoolean("isFirstOpen", false).apply()
        }

        Log.d(TAG, "onCreate: Main activity is being created")

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupPermissionLauncher()
        checkPermissionsAndStartLocationUpdates()
    }

    fun onNextButtonClick(view: View) {
        if (::latestLocation.isInitialized) {
            Toast.makeText(this, "Going to the second layer!", Toast.LENGTH_SHORT).show()
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
    fun onNextOSMButtonClick(view: View) {
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

    fun onNextSettingsButtonClick(view: View){
        Toast.makeText(this, "Going to the Settings", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

    private fun checkPermissionsAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        // Get last known location immediately
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        }
    }
    private fun requestLocationPermissions(){
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
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

    private fun retrieveUserPreferences() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        val authToken = sharedPreferences.getString("authToken", null)
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Use these values as needed in your app
    }

    private fun saveUserIdentifier(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }
    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    private fun askForUserIdentifier() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter User Identifier")
            .setIcon(R.mipmap.ic_launcher)
            .setView(input)
            .setPositiveButton("Save") { dialog, which ->
                val userInput = input.text.toString()
                if (userInput.isNotBlank()) {
                    saveUserIdentifier(userInput)
                    Toast.makeText(this, "User ID saved: $userInput", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "User ID cannot be blank", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCoordinatesToFile(latitude: Double, longitude: Double) {
        val fileName = "gps_coordinates.csv"
        val file = File(filesDir, fileName)
        val timestamp = System.currentTimeMillis()
        file.appendText("$timestamp;$latitude;$longitude\n")
    }


    override fun onLocationChanged(location: Location) {
        latestLocation = location
        runOnUiThread {
            val textView: TextView = findViewById(R.id.mainTextView)
            textView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        }
        saveCoordinatesToFile(location.latitude, location.longitude)
        Utils.writeLocationToCSV(this, location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
fun Greeting(name: String): String {
    return "Hello ${name}!"
}
