package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.mad_2024_app.database.User

@Dao
interface UserDAO {
    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM User")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUserById(userId: Int): User

    @Delete
    fun deleteUser(user: User)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM User WHERE userId = :userId")
    fun deleteUserById(userId: Int)

    // Other database operations as needed
}
