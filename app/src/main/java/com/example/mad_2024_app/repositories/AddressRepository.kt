package com.example.mad_2024_app.repositories

import com.example.mad_2024_app.DAOs.AddressDAO
import com.example.mad_2024_app.database.Address
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton

@Singleton
class AddressRepository(private val addressDAO: AddressDAO, private val cache: Cache<String, Any>) : IRepository {

    private val TAG: String = "AddressRepo"
    private val modelName: String = "Address"

    fun getAllAddresses(): Flow<List<Address>> = flow {
        // Check if addresses are present in cache
        val cachedAddresses = cache.getIfPresent("allAddresses") as List<Address>?
        if (cachedAddresses != null) {
            emit(cachedAddresses) // Emit cached addresses if present
        } else {
            // If addresses are not in cache, fetch from database and emit result
            val addresses = addressDAO.getAllAddresses().firstOrNull()
            addresses?.let {
                cache.put("allAddresses", it) // Cache the addresses if found
            }
            emit(addresses ?: emptyList()) // Emit addresses from database or an empty list if not found
        }
    }.flowOn(Dispatchers.IO)

    fun getAddressById(addressId: Int): Flow<Address?> = flow {
        // Check if address is present in cache
        val cachedAddress = cache.getIfPresent("$modelName@$addressId") as Address?
        if (cachedAddress != null) {
            emit(cachedAddress) // Emit cached address if present
        } else {
            // If address is not in cache, fetch from database and emit result
            val address = addressDAO.getAddressById(addressId).firstOrNull()
            address?.let {
                cache.put("$modelName@$addressId", it) // Cache the address if found
            }
            emit(address) // Emit address from database or null if not found
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertAddress(address: Address) : Long {
        val upsertedId = addressDAO.upsert(address)
        // Update cache after insertion
        if (upsertedId != -1L) {
            cache.put("$modelName@$upsertedId", address)
        }
        Utils.printCacheContents(TAG, cache)

        return upsertedId
    }

    suspend fun deleteAddress(address: Address) {
        addressDAO.delete(address)
        // Remove address from cache after deletion
        cache.invalidate("$modelName@${address.addressId}")
    }

    suspend fun deleteAddressById(addressId: Int) {
        addressDAO.deleteById(addressId)
        // Remove address from cache after deletion
        cache.invalidate("$modelName@$addressId")
    }

    fun getAddressByLocationId(locationId: Int): Flow<Address?> = flow {
        val address = addressDAO.getAddressByLocationId(locationId).firstOrNull()
        if (address != null) {
            cache.put(modelName+"@"+address.addressId.toString(), address)
        }
        emit(address)
    }.flowOn(Dispatchers.IO)

    // Additional methods as needed
}