package com.example.mad_2024_app.repositories

import android.util.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.sync.Mutex
import androidx.lifecycle.map as map

class UserRepository(private val userDao: UserDAO): Repository{
    private val cacheSize = 1024 * 1024 // 1MB for instance
    private val cache = LruCache<String, List<Shop>>(cacheSize)
    private val mutex = Mutex()
    private var lastCacheUpdateTime = System.currentTimeMillis()
    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun getUserById(userId: Int){
        userDao.getUserById(userId)
    }

    override fun clearCache() {
        cache.evictAll()
    }
}
