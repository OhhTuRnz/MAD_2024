package com.example.mad_2024_app.database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = User::class,
        parentColumns = arrayOf("userId"),
        childColumns = arrayOf("visitorId")),
    ForeignKey(entity = Shop::class,
        parentColumns = arrayOf("shopId"),
        childColumns = arrayOf("visitedShopId"))])
data class ShopVisitHistory(
    @PrimaryKey(autoGenerate = true) val visitHistoryId: Int = 0,
    val visitorId : Int,
    val visitedShopId : Int,
    val timestamp : Int
)