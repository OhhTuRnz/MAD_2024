package com.example.mad_2024_app.repositories

import com.example.mad_2024_app.DAOs.ShopVisitHistoryDAO
import com.example.mad_2024_app.database.ShopVisitHistory
import com.google.common.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Singleton

@Singleton
class ShopVisitHistoryRepository(
    private val shopVisitDAO: ShopVisitHistoryDAO,
    private val cache: Cache<String, Any>
) : IRepository {

    private val TAG = "ShopVisitHistoryRepo"
    private val modelName = "ShopVisit"

    fun getVisitsByUser(uuid: String): Flow<List<ShopVisitHistory>> = flow {
        val cacheKey = modelName + uuid.orEmpty()
        val cachedVisits = cache.getIfPresent(cacheKey) as List<ShopVisitHistory>?
        if (cachedVisits != null) {
            emit(cachedVisits)
        } else {
            val visits = shopVisitDAO.getVisitHistoryByUser(uuid).firstOrNull()
            visits?.let {
                cache.put(cacheKey, it)
                emit(it)
            } ?: emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsert(shopVisit: ShopVisitHistory) {
        val insertedId = shopVisitDAO.upsert(shopVisit)
        if (insertedId != -1L) {
            cache.put(modelName + insertedId.toString(), shopVisit)
        }
    }

    suspend fun removeVisit(shopVisit: ShopVisitHistory) {
        shopVisitDAO.deleteVisitHistory(shopVisit)
        cache.invalidate(modelName + shopVisit.visitorUuid)
    }

    suspend fun removeVisitById(visitorUuid: String, visitedShopId : Int, timestamp: Long) {
        shopVisitDAO.deleteVisitHistoryById(visitorUuid, visitedShopId, timestamp)
        cache.invalidate(modelName + visitorUuid + visitedShopId + timestamp)
    }

    suspend fun removeUserVisitHistory(visitorUuid : String) {
        shopVisitDAO.deleteUserVisitHistory(visitorUuid)
    }
}
