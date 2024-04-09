package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "FavoriteDonuts",
    primaryKeys = ["donutId", "uuid"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uuid"], childColumns = ["uuid"]),
        ForeignKey(entity = Donut::class, parentColumns = ["donutId"], childColumns = ["donutId"], onDelete= ForeignKey.CASCADE)
    ])
data class FavoriteDonuts (
    val donutId: Int,
    val uuid: String
)