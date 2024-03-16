package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.User

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User) : Long

    @Query("SELECT * FROM User")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUserById(userId: Int): User

    @Query("SELECT * FROM User WHERE uuid = :userUUID")
    fun getUserByUUID(userUUID: String) : User?

    @Delete
    fun delete(user: User)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM User WHERE userId = :userId")
    fun deleteUserById(userId: Int)

    // Other database operations as needed
}
