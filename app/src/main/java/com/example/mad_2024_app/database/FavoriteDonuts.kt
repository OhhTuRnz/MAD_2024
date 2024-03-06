package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "FavoriteDonuts",
    primaryKeys = ["donutId", "userId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["userId"], childColumns = ["userId"]),
        ForeignKey(entity = Shop::class, parentColumns = ["shopId"], childColumns = ["shopId"])
    ])
data class FavoriteDonuts (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val donutId: Int,
    val userId: Int
)