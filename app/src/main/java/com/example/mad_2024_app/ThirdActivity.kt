package com.example.mad_2024_app
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ThirdActivity : ComponentActivity() {

    private val TAG = "LogoGPS3rdActvity"
    private lateinit var latestLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val latitude = intent.getStringExtra("latitude")
        val longitude = intent.getStringExtra("longitude")
        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")

    }

    fun onPrevButtonClick(view: View){
        // This is the handler
        Toast.makeText(this, "Going to the second layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }
}
