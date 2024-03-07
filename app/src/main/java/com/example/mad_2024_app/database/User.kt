package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val email: String,
    val iconPath: String?, // This could be a path or a resource identifier,
    val uuid: String,
    val homeLatitude : Double? = null,
    val homeLongitude : Double? = null
)

