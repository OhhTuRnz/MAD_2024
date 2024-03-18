package com.example.mad_2024_app.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.common.cache.Cache
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDAO, private val cache: Cache<String, Any>) : IRepository{
    private val TAG : String = "User Cache"

    suspend fun insert(user: User) {
        // Insert user into the database
        userDao.insert(user)

        // Update cache after insertion
        cache.put(user.uuid, user)
    }

    suspend fun getUserById(userId: Int): LiveData<User?> = liveData(Dispatchers.IO){
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(userId.toString()) as User?
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userId")
            Log.i(TAG, "User in cache: ${cachedUser.toString()}")
            emit(cachedUser)
        }
        Log.d(TAG, "Cache miss for userUUID: $userId")
        // If user is not in cache, fetch from database
        val user = userDao.getUserById(userId)

        // Cache the user if found
        user?.let { cache.put(userId.toString(), it) }
        Log.d(TAG, "DatabaseInsertId: Adding user to cache with uuid: ${user.userId}")

        emit(user)
    }

    fun getUserByUUID(userUUID: String): LiveData<User?> = liveData(Dispatchers.IO){
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(userUUID) as User?
        printCacheContents()
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userUUID")
            Log.i(TAG, "User in cache: ${cachedUser.toString()}")
            emit(cachedUser)
        }
        Log.d(TAG, "Cache miss for userUUID: $userUUID")
        // If user is not in cache, fetch from database
        val user = userDao.getUserByUUID(userUUID = userUUID)

        // Cache the user if found
        user?.let { cache.put(userUUID, it) }
        Log.d(TAG, "DatabaseInsertUUID: Adding user to cache with uuid: ${user?.uuid}")
        printCacheContents()
        if (user != null) {
            emit(user)
        }
    }

    private suspend fun printCacheContents() = withContext(Dispatchers.IO) {
        val cacheContents = cache.asMap()
        Log.d(TAG, "Cache Contents:")

        cacheContents.forEach { (key, value) ->
            Log.d(TAG, "Key: $key, Value: $value")
        }
    }
}
