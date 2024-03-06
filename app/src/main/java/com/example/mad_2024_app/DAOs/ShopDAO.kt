package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mad_2024_app.database.Shop

@Dao
interface ShopDAO {
    @Insert
    fun insert(shop: Shop)

    @Query("SELECT * FROM Shop")
    fun getAllShops(): List<Shop>

    @Query("SELECT * FROM User WHERE id = :userId")
    fun getShopById(shopId: Int): Shop

    // Other database operations as needed
}
