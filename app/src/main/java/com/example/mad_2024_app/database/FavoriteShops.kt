package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["uuid", "shopId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uuid"], childColumns = ["uuid"]),
        ForeignKey(entity = Shop::class, parentColumns = ["shopId"], childColumns = ["shopId"], onDelete= ForeignKey.CASCADE)
    ])
data class FavoriteShops(
    val uuid: String,
    val shopId: Int
)
