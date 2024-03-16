package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Coordinate

@Dao
interface CoordinateDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(coordinate: Coordinate) : Long

    @Query("SELECT * FROM Coordinate")
    fun getAllCoordinates(): LiveData<List<Coordinate>>

    @Query("SELECT * FROM Coordinate WHERE coordinateId = :coordinateId")
    fun getCoordinateById(coordinateId: Int): Coordinate

    @Delete
    fun delete(coordinate: Coordinate)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Coordinate WHERE coordinateId = :coordinateId")
    fun deleteById(coordinateId: Int)
}
