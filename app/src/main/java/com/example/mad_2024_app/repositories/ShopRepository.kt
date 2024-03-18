package com.example.mad_2024_app.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class ShopRepository(private val shopDao: ShopDAO, private val cache: Cache<String, Any>) : IRepository{
    private val TAG = "ShopRepo"
    // Method to insert a shop
    suspend fun insert(shop: Shop){
        shopDao.insert(shop)
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

    fun getAllShopsNearCoordinates(location: Coordinate, radius: Int = 5000): LiveData<List<Shop>> = liveData(Dispatchers.IO) {
        val cacheKey = "${location.latitude},${location.longitude},$radius"

        // Check cache
        val cachedShops = cache.getIfPresent(cacheKey) as? List<Shop>
        if (cachedShops != null) {
            emit(cachedShops)
        } else {
            val center = PointF(location.latitude, location.longitude)
            val mult = 1.1 // mult 1.1 is more reliable

            // Calculate bounding box points
            val minLat = calculateDerivedPosition(center, mult * radius, 180.0).x
            val maxLat = calculateDerivedPosition(center, mult * radius, 0.0).x
            // Applying the fudge factor for longitude because not in the equator
            val fudge = cos(Math.toRadians(location.latitude)).pow(2)
            val minLon = calculateDerivedPosition(center, mult * radius * fudge, 270.0).y
            val maxLon = calculateDerivedPosition(center, mult * radius * fudge, 90.0).y


            Log.d(TAG, "ShopsNearCoordinates: SELECT * FROM Shop WHERE locationId IN (SELECT coordinateId FROM Coordinate WHERE latitude BETWEEN :$minLat AND :$maxLat AND longitude BETWEEN :$minLon AND :$maxLon)")

            // Fetch from database
            val shops = shopDao.getShopsWithinBounds(minLat, maxLat, minLon, maxLon)


            // Cache the result and emit
            shops?.let {
                cache.put(cacheKey, it)
                Log.d(TAG, "Emitting shops nearby")
                emit(it)
            } ?: emit(emptyList())
        }
    }

    fun getAllShops(): LiveData<List<Shop>> {
        return shopDao.getAllShops()
    }
}
