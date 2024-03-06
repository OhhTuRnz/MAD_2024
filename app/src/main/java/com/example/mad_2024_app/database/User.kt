package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val username: String,
    val email: String,
    val iconPath: String, // This could be a path or a resource identifier
    val homeLatitude : Double,
    val homeLongitude : Double
)

