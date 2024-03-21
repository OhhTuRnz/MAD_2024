package com.example.mad_2024_app.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.common.cache.Cache
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDAO, private val cache: Cache<String, Any>) : IRepository{
    private val TAG : String = "UserRepo"
    private val modelName = "User"
    suspend fun insert(user: User) {
        // Insert user into the database
        userDao.insert(user)

        // Update cache after insertion
        cache.put(modelName+user.uuid, user)
    }

    fun getUserById(userId: Int): Flow<User?> = flow {
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(modelName+userId.toString()) as User?
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userId: $userId")
            emit(cachedUser) // Emit cached user
        } else {
            Log.d(TAG, "Cache miss for userId: $userId")
            // If user is not in cache, fetch from database and emit result
            val user = userDao.getUserById(userId).firstOrNull()
            user?.let {
                cache.put(modelName+userId.toString(), it) // Cache the user if found
                Log.d(TAG, "DatabaseInsertId: Adding user to cache with userId: ${it.userId}")
            }
            emit(user) // Emit user from database or null if not found
        }
    }.flowOn(Dispatchers.IO)

    fun getUserByUUID(userUUID: String): Flow<User?> = flow {
        // Check if user is present in cache
        val cachedUser = cache.getIfPresent(modelName+userUUID) as User?
        if (cachedUser != null) {
            Log.d(TAG, "Cache hit for userUUID: $userUUID")
            emit(cachedUser) // Emit cached user
        } else {
            Log.d(TAG, "Cache miss for userUUID: $userUUID")
            // If user is not in cache, fetch from database and emit result
            val user = userDao.getUserByUUID(userUUID).firstOrNull()
            user?.let {
                cache.put(modelName+userUUID, it) // Cache the user if found
                Log.d(TAG, "DatabaseInsertUUID: Adding user to cache with uuid: ${it.uuid}")
            }
            emit(user) // Emit user from database or null if not found
        }
    }.flowOn(Dispatchers.IO)
}
