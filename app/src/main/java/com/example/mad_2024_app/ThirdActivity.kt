package com.example.mad_2024_app
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
    }

    fun onPrevButtonClick(view: View){
        // This is the handler
        Toast.makeText(this, "Going to the second layer!", Toast.LENGTH_SHORT).show()

        // go to another activity
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }

}
