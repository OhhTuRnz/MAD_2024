package com.example.mad_2024_app.repositories

import android.util.Log
import com.example.mad_2024_app.DAOs.CoordinateDAO
import com.example.mad_2024_app.database.Coordinate
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton

@Singleton
class CoordinateRepository(private val coordinateDAO: CoordinateDAO, private val cache: Cache<String, Any>) : IRepository {
    private val TAG: String = "CoordinateRepo"
    private val modelName : String = "Coordinate"

    fun getAllCoordinates(): Flow<List<Coordinate>> = flow {
        // Check if addresses are present in cache
        val cachedAddresses = cache.getIfPresent("allCoordinates") as List<Coordinate>?
        if (cachedAddresses != null) {
            emit(cachedAddresses) // Emit cached addresses if present
        } else {
            // If addresses are not in cache, fetch from database and emit result
            val addresses = coordinateDAO.getAllCoordinates().firstOrNull()
            addresses?.let {
                cache.put("allCoordinates", it) // Cache the addresses if found
            }
            emit(addresses ?: emptyList()) // Emit addresses from database or an empty list if not found
        }
    }.flowOn(Dispatchers.IO)

    fun getCoordinateById(addressId: Int): Flow<Coordinate?> = flow {
        val cachedAddress = cache.getIfPresent(modelName+addressId.toString()) as Coordinate?
        if (cachedAddress != null) {
            emit(cachedAddress)
        } else {
            val address = coordinateDAO.getCoordinateById(addressId).firstOrNull()
            address?.let {
                cache.put(modelName+addressId.toString(), it)
            }
            emit(address)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertCoordinate(coordinate: Coordinate) : Long {
        val upsertedId = coordinateDAO.upsert(coordinate)
        if (upsertedId != -1L) {
            cache.put(modelName + upsertedId.toString(), coordinate)
        }
        return upsertedId
    }

    suspend fun deleteCoordinate(coordinate: Coordinate) {
        coordinateDAO.delete(coordinate)
        // Remove address from cache after deletion
        cache.invalidate(modelName+coordinate.coordinateId.toString())
    }

    suspend fun deleteCoordinateById(addressId: Int) {
        coordinateDAO.deleteById(addressId)
        // Remove address from cache after deletion
        cache.invalidate(modelName+addressId.toString())
    }

    fun getCoordinateByLatitudeAndLongitude(latitude: Double, longitude: Double): Flow<Coordinate?> = flow {
        val coordinate = coordinateDAO.getCoordinateByLatitudeAndLongitude(latitude, longitude).firstOrNull()
        coordinate?.let {
            Log.d(TAG, "Coordinate found with ID: ${it.coordinateId}")
            // Use the coordinate ID as part of the cache key
            val cacheKey = "$modelName${it.coordinateId}"
            val cachedCoordinate = cache.getIfPresent(cacheKey) as Coordinate?

            if (cachedCoordinate != null) {
                emit(cachedCoordinate) // Emit the cached coordinate
            } else {
                cache.put(cacheKey, it) // Cache the new coordinate
                emit(it) // Emit the new coordinate
            }
        } ?: run {
            Log.d(TAG, "No coordinate found for latitude: $latitude and longitude: $longitude")
            emit(null) // Emit null if the coordinate is not found
        }
    }.flowOn(Dispatchers.IO)

}