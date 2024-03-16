package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop

@Dao
interface ShopDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(shop: Shop) : Long

    @Query("SELECT * FROM Shop")
    fun getAllShops(): LiveData<List<Shop>>

    @Query("SELECT * FROM Shop WHERE shopId = :shopId")
    fun getShopById(shopId: Int): Shop

    @Delete
    fun delete(shop: Shop)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Shop WHERE shopId = :shopId")
    fun deleteById(shopId: Int)

    @Query("SELECT * FROM Shop INNER JOIN Coordinate ON Shop.locationId = Coordinate.coordinateId WHERE Coordinate.latitude BETWEEN :minLat AND :maxLat AND Coordinate.longitude BETWEEN :minLon AND :maxLon")
    fun getShopsWithinBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): LiveData<List<Shop>>

}
