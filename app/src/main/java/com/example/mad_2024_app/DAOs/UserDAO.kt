package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {
    @Upsert
    suspend fun upsert(user: User): Long

    @Query("SELECT * FROM User")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUserById(userId: Int): Flow<User>

    @Query("SELECT * FROM User WHERE uuid = :userUUID")
    fun getUserByUUID(userUUID: String): Flow<User?>

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM User WHERE userId = :userId")
    suspend fun deleteUserById(userId: Int)

    // Other database operations as needed
}
