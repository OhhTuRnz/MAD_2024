package com.example.mad_2024_app.database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("uuid"),
            childColumns = arrayOf("visitorUuid"),
        ),
        ForeignKey(
            entity = Shop::class,
            parentColumns = arrayOf("shopId"),
            childColumns = arrayOf("visitedShopId"),
        )
    ],
    primaryKeys = ["visitorUuid", "visitedShopId", "timestamp"]
)
data class ShopVisitHistory(
    val visitorUuid : String,
    val visitedShopId : Int,
    val timestamp : Long
)