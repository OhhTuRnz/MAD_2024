package com.example.mad_2024_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Donut(
    @PrimaryKey(autoGenerate = true) val donutId: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    val type: String,
    val color: String? = null,
    val image: Int = 0
)