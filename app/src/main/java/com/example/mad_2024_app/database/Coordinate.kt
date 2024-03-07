package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Coordinate(
    @PrimaryKey(autoGenerate = true) val coordinateId: Int = 0,
    val latitude: Double,
    val longitude: Double
)