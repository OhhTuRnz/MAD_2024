package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Donut(
    @PrimaryKey(autoGenerate = true) val donutId: Int = 0,
    val name: String,
    val type: String,
    val color: String? = null
)