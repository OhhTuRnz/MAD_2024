package com.example.mad_2024_app.repositories

import android.util.Log
import com.example.mad_2024_app.DAOs.FavoriteShopsDAO
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton

@Singleton
class FavoriteShopsRepository(private val favoriteShopsDAO: FavoriteShopsDAO, private val cache: Cache<String, Any>) : IRepository{

    private val TAG: String = "FavoriteShopsRepo"
    private val modelName: String = "FavoriteShops"

    fun getFavoriteShopsByUser(uuid: String?): Flow<List<Shop>> = flow {
        val cachedShops = cache.getIfPresent("$modelName@$uuid") as List<Shop>?

        if (cachedShops != null) {
            emit(cachedShops)
        } else {
            val shops = favoriteShopsDAO.getFavoriteShopsByUser(uuid).firstOrNull()
            shops?.let {
                cache.put("$modelName@$uuid", it)
            }
            emit(shops ?: emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertFavoriteShop(favoriteShop: FavoriteShops) {
        val upsertedId = favoriteShopsDAO.upsert(favoriteShop)
        // If it's a new insert, the DAO will return the new row ID. If it's an update, it'll return the ID of the updated row.
        if (upsertedId != -1L) {
            cache.put("$modelName@${favoriteShop.uuid}@${favoriteShop.shopId}", favoriteShop)
        }

        Utils.printCacheContents(TAG, cache)
    }

    suspend fun removeFavoriteShop(favoriteShop: FavoriteShops) {
        favoriteShopsDAO.removeFavoriteShop(favoriteShop)
        cache.invalidate("$modelName@${favoriteShop.uuid}@${favoriteShop.shopId}")
    }

    suspend fun removeFavoriteShopById(uuid: String?, shopId: Int) {
        Log.d(TAG, "Removing shop with id $shopId for user with uuid: $uuid")
        favoriteShopsDAO.removeFavoriteShopById(uuid, shopId)
        cache.invalidate("$modelName@$uuid@$shopId")
        cache.invalidate("$modelName@$uuid")
    }

    suspend fun isShopFavorite(uuid: String?, shopId: Int): Boolean {
        val cacheKey = modelName+"@"+uuid.toString()+"@"+shopId.toString()
        val cachedValue = cache.getIfPresent(cacheKey) as Boolean?

        return if (cachedValue != null) {
            cachedValue
        } else {
            val isFavorite = favoriteShopsDAO.isShopFavorite(uuid, shopId)
            cache.put(cacheKey, isFavorite)
            isFavorite
        }
    }
}