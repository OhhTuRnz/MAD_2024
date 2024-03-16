package com.example.mad_2024_app.repositories

import android.util.Log
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.User
import java.util.concurrent.TimeUnit

class UserRepository(private val userDao: UserDAO) {
    private val cache: Cache<String, User> = CacheBuilder.newBuilder()
        .maximumSize(100) // Maximum cache size
        .expireAfterWrite(10, TimeUnit.MINUTES) // Cache expiration time
        .build()

    private val TAG : String = "User Cache"

    suspend fun insert(user: User) {
        // Insert user into the database
        userDao.insert(user)

        // Update cache after insertion
        cache.put(user.uuid, user)
    }

    suspend fun getUserById(userId: Int): User {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByUUID(userUUID: String): User? {
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(userUUID)
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userUUID")
            return cachedUser
        } else {
            Log.d(TAG, "Cache miss for userUUID: $userUUID")
        }

        // If user is not in cache, fetch from database
        val user = userDao.getUserByUUID(userUUID)

        // Cache the user if found
        user?.let { cache.put(userUUID, it) }

        return user
    }
}
