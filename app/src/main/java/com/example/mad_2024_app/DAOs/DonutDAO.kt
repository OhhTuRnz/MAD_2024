package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Donut

@Dao
interface DonutDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(donut: Donut) : Long

    @Query("SELECT * FROM Donut")
    fun getAllDonuts(): LiveData<List<Donut>>

    @Query("SELECT * FROM Donut WHERE donutId = :donutId")
    fun getDonutById(donutId: Int): Donut

    @Delete
    fun delete(donut: Donut)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Donut WHERE donutId = :donutId")
    fun deleteById(donutId: Int)
}
