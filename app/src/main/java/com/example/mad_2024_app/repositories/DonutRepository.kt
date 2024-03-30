package com.example.mad_2024_app.repositories

import androidx.lifecycle.LiveData
import com.example.mad_2024_app.DAOs.DonutDAO
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.Shop
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DonutRepository(private val donutsDAO: DonutDAO, private val cache: Cache<String, Any>) : IRepository {

    private val TAG: String = "DonutsRepo"
    private val modelName: String = "Donut"

    fun getAllDonuts(): LiveData<List<Donut>> = donutsDAO.getAllDonuts()

    fun getDonutById(donutId: Int): Flow<Donut?> = flow {
        // Check if donut is present in cache
        val cachedDonut = cache.getIfPresent(modelName + donutId.toString()) as Donut?
        if (cachedDonut != null) {
            emit(cachedDonut) // Emit cached donut if present
        } else {
            // If donut is not in cache, fetch from database and emit result
            val donut = donutsDAO.getDonutById(donutId).firstOrNull()
            donut?.let {
                cache.put(modelName + donutId.toString(), it) // Cache the donut if found
            }
            emit(donut) // Emit donut from database or null if not found
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertDonut(donut: Donut) {
        val upsertedId = donutsDAO.upsert(donut)
        // Update cache after insertion
        if (upsertedId != -1L) {
            cache.put(modelName + upsertedId.toString(), donut)
        }
        Utils.printCacheContents(TAG, cache) // Assuming Utils.printCacheContents exists for debugging
    }

    suspend fun deleteDonut(donut: Donut) {
        donutsDAO.delete(donut)
        // Remove donut from cache after deletion
        cache.invalidate(modelName + donut.donutId.toString())
    }

    suspend fun deleteById(donutId: Int) {
        donutsDAO.deleteById(donutId)
        // Remove donut from cache after deletion
        cache.invalidate(modelName + donutId.toString())
    }

    // Additional methods as needed (e.g., filtering by specific criteria)
}
