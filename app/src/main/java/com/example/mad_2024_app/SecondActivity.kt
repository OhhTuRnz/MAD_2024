package com.example.mad_2024_app
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {

    private val TAG = "LogoGPS2ndActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

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

}


