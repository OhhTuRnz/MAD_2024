package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

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
