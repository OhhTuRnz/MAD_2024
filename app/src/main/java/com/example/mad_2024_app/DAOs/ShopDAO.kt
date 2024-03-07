package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.mad_2024_app.database.Shop

@Dao
interface ShopDAO {
    @Insert
    fun insert(shop: Shop)

    @Query("SELECT * FROM Shop")
    fun getAllShops(): List<Shop>

    @Query("SELECT * FROM Shop WHERE shopId = :shopId")
    fun getShopById(shopId: Int): Shop

    @Delete
    fun delete(shop: Shop)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Shop WHERE shopId = :shopId")
    fun deleteById(shopId: Int)

    // Other database operations as needed
}
