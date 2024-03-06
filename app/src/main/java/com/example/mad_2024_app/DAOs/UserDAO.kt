package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mad_2024_app.database.User

@Dao
interface UserDAO {
    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM User")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE id = :userId")
    fun getUserById(userId: Int): User

    // Other database operations as needed
}
