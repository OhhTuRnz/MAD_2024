package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mad_2024_app.database.Donut

@Dao
interface DonutDAO {
    @Insert
    fun insert(donut: Donut)

    @Query("SELECT * FROM Donut")
    fun getAllDonuts(): List<Donut>

    @Query("SELECT * FROM Donut WHERE id = :donutId")
    fun getDonutById(donutId: Int): Donut

    // Other database operations as needed
}
