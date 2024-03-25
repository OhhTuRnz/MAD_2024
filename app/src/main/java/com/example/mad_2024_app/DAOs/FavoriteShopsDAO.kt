package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteShopsDAO {
    @Upsert
    suspend fun upsert(favoriteShop: FavoriteShops) : Long

    @Delete
    suspend fun removeFavoriteShop(favoriteShop: FavoriteShops)

    @Query("DELETE FROM FavoriteShops WHERE uuid = :uuid AND shopId = :shopId")
    suspend fun removeFavoriteShopById(uuid: String?, shopId: Int)

    @Query("SELECT * FROM Shop INNER JOIN FavoriteShops ON Shop.shopId = FavoriteShops.shopId WHERE FavoriteShops.uuid = :uuid")
    fun getFavoriteShopsByUser(uuid: String?): Flow<List<Shop>>

    @Query("SELECT COUNT(*) > 0 FROM FavoriteShops WHERE uuid = :uuid AND shopId = :shopId")
    suspend fun isShopFavorite(uuid: String?, shopId: Int): Boolean
}
