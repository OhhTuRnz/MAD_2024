package com.example.mad_2024_app.repositories

import android.graphics.Bitmap
import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.TimeUnit
import kotlin.math.cos

class ShopRepository(private val shopDao: ShopDAO, private val cache: Cache<String, Any>) : IRepository{
    // Method to insert a shop
    suspend fun insert(shop: Shop){
        shopDao.insert(shop)
    }

    // Method to retrieve all shops near coordinates
    suspend fun getAllShopsNearCoordinates(location: Coordinate, radius: Int = 500): LiveData<List<Shop>> = liveData(
        Dispatchers.IO) {
        val cacheKey = "${location.latitude},${location.longitude},$radius"

        // Check cache
        val cachedShops = cache.getIfPresent(cacheKey) as? List<Shop>
        if (cachedShops != null) {
            emit(cachedShops)
        } else {
            val latChange = radius / 111000
            val lonChange = radius / (111000 * cos(location.latitude * Math.PI / 180))

            val minLat = location.latitude - latChange
            val maxLat = location.latitude + latChange
            val minLon = location.longitude - lonChange
            val maxLon = location.longitude + lonChange

            // Fetch from database
            val result = shopDao.getShopsWithinBounds(minLat, maxLat, minLon, maxLon).value

            // Update cache and emit result
            result?.let { shops ->
                cache.put(cacheKey, shops)
                emit(shops)
            } ?: emit(emptyList())
        }
    }
}
