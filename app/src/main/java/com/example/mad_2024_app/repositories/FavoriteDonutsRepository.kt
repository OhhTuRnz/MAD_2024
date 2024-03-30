package com.example.mad_2024_app.repositories

import android.util.Log
import com.example.mad_2024_app.DAOs.FavoriteDonutsDAO
import com.example.mad_2024_app.database.FavoriteDonuts
import com.example.mad_2024_app.database.Donut
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FavoriteDonutsRepository(private val favoriteDonutsDAO: FavoriteDonutsDAO, private val cache: Cache<String, Any>) : IRepository {

    private val TAG: String = "FavoriteDonutsRepo"
    private val modelName: String = "FavoriteDonuts"

    fun getFavoriteDonutsByUser(userId: String?): Flow<List<Donut>> = flow {
        val cachedDonuts = cache.getIfPresent("$modelName@$userId") as List<Donut>?
        if (cachedDonuts != null) {
            emit(cachedDonuts)
        } else {
            val donuts = favoriteDonutsDAO.getFavoriteDonutsByUser(userId).firstOrNull()
            donuts?.let {
                cache.put("$modelName@$userId", it)
            }
            emit(donuts ?: emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertFavoriteDonut(favoriteDonut: FavoriteDonuts) {
        val upsertedId = favoriteDonutsDAO.upsert(favoriteDonut)
        // If it's a new insert, the DAO will return the new row ID. If it's an update, it'll return the ID of the updated row.
        if (upsertedId != -1L) {
            cache.put("$modelName@$upsertedId", favoriteDonut)
        }
    }

    suspend fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts) {
        favoriteDonutsDAO.removeFavoriteDonut(favoriteDonut)
        cache.invalidate("$modelName@${favoriteDonut.userId}")
    }

    suspend fun removeFavoriteDonutById(userId: Int, donutId: Int) {
        Log.d(TAG, "Removing donut with id $donutId for user with id: $userId")
        favoriteDonutsDAO.removeFavoriteDonutById(userId, donutId)
        cache.invalidate("$modelName@$userId@donutId")
    }

    suspend fun isDonutFavorite(userId: Int, donutId: Int): Boolean {
        val cacheKey = "$modelName@$userId@$donutId"
        val cachedValue = cache.getIfPresent(cacheKey) as Boolean?

        return if (cachedValue != null) {
            cachedValue
        } else {
            val isFavorite = favoriteDonutsDAO.isFavorite(userId, donutId)
            cache.put(cacheKey, isFavorite)
            isFavorite
        }
    }
}