package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Coordinate(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val latitude: Double,
    val longitude: Double
)