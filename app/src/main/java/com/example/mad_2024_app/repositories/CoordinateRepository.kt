package com.example.mad_2024_app.repositories

import com.example.mad_2024_app.DAOs.CoordinateDAO
import com.example.mad_2024_app.database.Coordinate
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
class CoordinateRepository(private val coordinateDAO: CoordinateDAO, private val cache: Cache<String, Any>) : IRepository {
    private val TAG: String = "CoordinateRepo"
    private val modelName : String = "Coordinate"

    fun getAllAddresses(): Flow<List<Coordinate>> = flow {
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

    fun getAddressById(addressId: Int): Flow<Coordinate?> = flow {
        // Check if address is present in cache
        val cachedAddress = cache.getIfPresent(modelName+addressId.toString()) as Coordinate?
        if (cachedAddress != null) {
            emit(cachedAddress) // Emit cached address if present
        } else {
            // If address is not in cache, fetch from database and emit result
            val address = coordinateDAO.getCoordinateById(addressId).firstOrNull()
            address?.let {
                cache.put(modelName+addressId.toString(), it) // Cache the address if found
            }
            emit(address) // Emit address from database or null if not found
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertAddress(coordinate: Coordinate) {
        val upsertedId = coordinateDAO.upsert(coordinate)
        if (upsertedId != -1L) {
            cache.put(modelName + upsertedId.toString(), coordinate)
        }
    }

    suspend fun deleteAddress(coordinate: Coordinate) {
        coordinateDAO.delete(coordinate)
        // Remove address from cache after deletion
        cache.invalidate(modelName+coordinate.coordinateId.toString())
    }

    suspend fun deleteAddressById(addressId: Int) {
        coordinateDAO.deleteById(addressId)
        // Remove address from cache after deletion
        cache.invalidate(modelName+addressId.toString())
    }
}