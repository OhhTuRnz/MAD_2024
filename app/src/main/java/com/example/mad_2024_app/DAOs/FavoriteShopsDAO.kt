package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop

@Dao
interface FavoriteShopsDAO {
    @Insert
    fun addFavoriteShop(favoriteShop: FavoriteShops)

    @Delete
    fun removeFavoriteShop(favoriteShop: FavoriteShops)

    @Query("SELECT * FROM Shop INNER JOIN FavoriteShops ON Shop.id = FavoriteShops.shopId WHERE FavoriteShops.userId = :userId")
    fun getFavoriteShopsByUser(userId: Int): List<Shop>

    // Other database operations as needed
}
