package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["userId", "shopId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["userId"], childColumns = ["userId"]),
        ForeignKey(entity = Shop::class, parentColumns = ["shopId"], childColumns = ["shopId"], onDelete= ForeignKey.CASCADE)
    ])
data class FavoriteShops(
    val userId: Int,
    val shopId: Int
)
