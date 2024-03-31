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

    fun getFavoriteDonutsByUser(uuid: String): Flow<List<Donut>> = flow {
        val cachedDonuts = cache.getIfPresent("$modelName@$uuid") as List<Donut>?
        if (cachedDonuts != null) {
            Log.d(TAG, "Returning cached donuts for user with id: $uuid: $cachedDonuts")
            emit(cachedDonuts)
        } else {
            val donuts = favoriteDonutsDAO.getFavoriteDonutsByUser(uuid).firstOrNull()
            donuts?.let {
                Log.d(TAG, "Caching donuts for user with id: $uuid: $it")
                cache.put("$modelName@$uuid", it)
            }
            emit(donuts ?: emptyList())
        }
        Utils.printCacheContents(TAG, cache)
    }.flowOn(Dispatchers.IO)

    suspend fun upsertFavoriteDonut(favoriteDonut: FavoriteDonuts) {
        val upsertedId = favoriteDonutsDAO.upsert(favoriteDonut)
        Log.d(TAG, "Upserted favorite donut with id: $upsertedId")
        // If it's a new insert, the DAO will return the new row ID. If it's an update, it'll return the ID of the updated row.
        if (upsertedId != -1L) {
            cache.put("$modelName@${favoriteDonut.uuid}@${favoriteDonut.donutId}", favoriteDonut)
        }
    }

    suspend fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts) {
        favoriteDonutsDAO.removeFavoriteDonut(favoriteDonut)
        cache.invalidate("$modelName@${favoriteDonut.uuid}@${favoriteDonut.donutId}")
        cache.invalidate("$modelName@${favoriteDonut.uuid}")
    }

    suspend fun removeFavoriteDonutById(uuid: String, donutId: Int) {
        Log.d(TAG, "Removing donut with id $donutId for user with id: $uuid")
        favoriteDonutsDAO.removeFavoriteDonutById(uuid, donutId)
        cache.invalidate("$modelName@$uuid@$donutId")
        cache.invalidate("$modelName@$uuid")
    }

    suspend fun isDonutFavorite(uuid: String, donutId: Int): Boolean {
        val cacheKey = "$modelName@$uuid@$donutId"
        val cachedValue = cache.getIfPresent(cacheKey) as Boolean?

        return if (cachedValue != null) {
            cachedValue
        } else {
            val isFavorite = favoriteDonutsDAO.isFavorite(uuid, donutId)
            cache.put(cacheKey, isFavorite)
            isFavorite
        }
    }
}