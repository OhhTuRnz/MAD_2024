package com.example.mad_2024_app.repositories

import android.util.Log
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(userId.toString())
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userId")
            Log.i(TAG, "User in cache: ${cachedUser.toString()}")
            return cachedUser
        }
        Log.d(TAG, "Cache miss for userUUID: $userId")
        // If user is not in cache, fetch from database
        val user = userDao.getUserById(userId)

        // Cache the user if found
        user?.let { cache.put(userId.toString(), it) }
        Log.d(TAG, "Adding user to cache with uuid: $userId")

        return user
    }

    suspend fun getUserByUUID(userUUID: String): User? = withContext(Dispatchers.IO){
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(userUUID)
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userUUID")
            Log.i(TAG, "User in cache: ${cachedUser.toString()}")
            return@withContext cachedUser
        }
        Log.d(TAG, "Cache miss for userUUID: $userUUID")
        // If user is not in cache, fetch from database
        val user = userDao.getUserByUUID(userUUID)

        // Cache the user if found
        user?.let { cache.put(userUUID, it) }
        Log.d(TAG, "Adding user to cache with uuid: $userUUID")

        return@withContext user
    }
}
