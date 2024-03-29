package com.example.mad_2024_app.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.User
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Singleton
class ShopRepository(private val shopDao: ShopDAO, private val cache: Cache<String, Any>) : IRepository{
    private val TAG = "ShopRepo"
    private val modelName = "Shop"

    // Method to insert a shop
    suspend fun upsert(shop: Shop){
        val upsertedId = shopDao.upsert(shop)

        if (upsertedId != -1L) {
            cache.put(modelName + upsertedId.toString(), shop)
        }
    }

    data class PointF(val x: Double, val y: Double)

    fun calculateDerivedPosition(point: PointF, range: Double, bearing: Double): PointF {
        val EarthRadius = 6371000.0 // Radius in meters

        val latA = Math.toRadians(point.x)
        val lonA = Math.toRadians(point.y)
        val angularDistance = range / EarthRadius
        val trueCourse = Math.toRadians(bearing)

        val lat = asin(sin(latA) * cos(angularDistance) + cos(latA) * sin(angularDistance) * cos(trueCourse))

        val dlon = atan2(sin(trueCourse) * sin(angularDistance) * cos(latA),
            cos(angularDistance) - sin(latA) * sin(lat))

        val lon = (lonA + dlon + Math.PI) % (Math.PI * 2) - Math.PI

        return PointF(Math.toDegrees(lat), Math.toDegrees(lon))
    }

    fun getAllShopsNearCoordinates(location: Coordinate, radius: Int = 5000): Flow<List<Shop>> = flow {
        val cacheKey = "${location.latitude},${location.longitude},$radius"

        // Check cache
        val cachedShops = cache.getIfPresent(modelName+cacheKey) as? List<Shop>
        if (cachedShops != null) {
            emit(cachedShops)
        } else {
            val center = PointF(location.latitude, location.longitude)
            val mult = 1.1 // mult 1.1 is more reliable

            // Calculate bounding box points
            val minLat = calculateDerivedPosition(center, mult * radius, 180.0).x
            val maxLat = calculateDerivedPosition(center, mult * radius, 0.0).x
            // Applying the fudge factor for longitude because not at the equator
            val fudge = cos(Math.toRadians(location.latitude)).pow(2)
            val minLon = calculateDerivedPosition(center, mult * radius * fudge, 270.0).y
            val maxLon = calculateDerivedPosition(center, mult * radius * fudge, 90.0).y

            // Fetch from database
            val shops = shopDao.getShopsWithinBounds(minLat, maxLat, minLon, maxLon).firstOrNull() ?: emptyList()

            // Cache the result and emit
            cache.put(modelName+cacheKey, shops)
            emit(shops)
        }
    }.flowOn(Dispatchers.IO) // Perform the flow operations on the IO dispatcher

    /*fun getAllShops(): Flow<List<Shop>> {
        return shopDao.getAllShops()
    }*/
    fun getAllShops(): LiveData<List<Shop>> = shopDao.getAllShops()

    fun getShopById(shopId: Int): Flow<Shop?> = flow {
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(modelName+shopId) as Shop?
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $shopId")
            emit(cachedUser) // Emit cached user
        } else {
            Log.d(TAG, "Cache miss for userUUID: $shopId")
            // If user is not in cache, fetch from database and emit result
            val shop = shopDao.getShopById(shopId).firstOrNull()
            shop?.let {
                cache.put(modelName+shopId, it) // Cache the user if found
                Log.d(TAG, "DatabaseInsertUUID: Adding user to cache with uuid: ${it.shopId}")
            }
            emit(shop) // Emit user from database or null if not found
        }
    }.flowOn(Dispatchers.IO)

    fun getShopByLocationId(locationId: Int): Flow<Shop?> = flow {
        val shop = shopDao.getShopByLocationId(locationId).firstOrNull()
        emit(shop)
    }.flowOn(Dispatchers.IO)
}
