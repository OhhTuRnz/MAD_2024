package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.ShopVisitHistory

@Dao
interface ShopVisitHistoryDAO {
    @Upsert
    fun upsert(visitHistory: ShopVisitHistory) : Long

    @Query("SELECT * FROM ShopVisitHistory WHERE visitorId = :userId ORDER BY timestamp DESC")
    fun getVisitHistoryByUser(userId: Int): List<ShopVisitHistory>

    @Query("SELECT * FROM ShopVisitHistory WHERE visitorId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLastVisitByUser(userId: Int): ShopVisitHistory?

    @Delete
    fun deleteVisitHistory(visitHistory: ShopVisitHistory)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM ShopVisitHistory WHERE visitHistoryId = :visitHistoryId")
    fun deleteVisitHistoryById(visitHistoryId: Int)

    // Additional queries as needed
}
