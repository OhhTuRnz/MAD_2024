package com.example.mad_2024_app
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val buttonNext = findViewById<Button>(R.id.next)
        val buttonUndo = findViewById<Button>(R.id.undo)

        buttonNext.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }
        buttonUndo.setOnClickListener {
            finish()
        }

    }

}
