package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(shop: Shop): Long

    @Query("SELECT * FROM Shop")
    fun getAllShops(): Flow<List<Shop>>

    @Query("SELECT * FROM Shop WHERE shopId = :shopId")
    fun getShopById(shopId: Int): Flow<Shop>

    @Delete
    suspend fun delete(shop: Shop)

    @Query("DELETE FROM Shop WHERE shopId = :shopId")
    suspend fun deleteById(shopId: Int)

    @Query("SELECT * FROM Shop WHERE locationId IN (SELECT coordinateId FROM Coordinate WHERE latitude >= :minLat AND latitude <= :maxLat AND longitude >= :minLon AND longitude <= :maxLon)")
    fun getShopsWithinBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<Shop>>
}
