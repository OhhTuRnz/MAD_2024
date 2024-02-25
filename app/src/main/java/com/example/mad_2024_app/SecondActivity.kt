package com.example.mad_2024_app
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import java.io.IOException

class SecondActivity : ComponentActivity() {

    private val TAG = "LogoGPS2ndActivity"
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location", Location::class.java)
        val tvFileContents: TextView = findViewById(R.id.tvFileContents)
        tvFileContents.text = readFileContents()

        if(location!=null){
            Log.i(TAG, "onCreate: Location[" + location.altitude + "][" + location.latitude + "][" + location.longitude + "]")
        }
    }

    fun onNextButtonClick(view: View) {
        // This is the handler
        Toast.makeText(this, "Going to the third layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, ThirdActivity::class.java)
        startActivity(intent)
    }

    fun onPrevButtonClick(view: View){
        // This is the handler
        Toast.makeText(this, "Going to the main layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun readFileContents(): String {
        val fileName = "gps_coordinates.csv"
        return try {
            // Open the file from internal storage
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }
        } catch (e: IOException) {
            "Error reading file: ${e.message}"
        }
    }


}


