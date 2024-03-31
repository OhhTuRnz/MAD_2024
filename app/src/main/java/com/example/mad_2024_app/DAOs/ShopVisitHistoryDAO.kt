package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.ShopVisitHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopVisitHistoryDAO {
    @Upsert
    fun upsert(visitHistory: ShopVisitHistory) : Long

    @Query("SELECT * FROM ShopVisitHistory WHERE  visitorUuid = :visitorUuid ORDER BY timestamp DESC")
    fun getVisitHistoryByUser(visitorUuid: String): Flow<List<ShopVisitHistory>>

    @Query("SELECT * FROM ShopVisitHistory WHERE visitorUuid = :visitorUuid ORDER BY timestamp DESC LIMIT 1")
    fun getLastVisitByUser(visitorUuid: String): Flow<ShopVisitHistory>

    @Delete
    fun deleteVisitHistory(visitHistory: ShopVisitHistory)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM ShopVisitHistory WHERE visitorUuid = :visitorUuid AND visitedShopId = :shopId AND timestamp = :timestamp")
    fun deleteVisitHistoryById(visitorUuid: String, shopId : Int, timestamp: Long)

    @Query("DELETE FROM ShopVisitHistory WHERE visitorUuid = :uuid")
    fun deleteUserVisitHistory(uuid: String)

    // Additional queries as needed
}
