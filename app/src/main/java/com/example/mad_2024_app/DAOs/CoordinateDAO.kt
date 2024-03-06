package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mad_2024_app.database.Coordinate

@Dao
interface CoordinateDAO {
    @Insert
    fun insert(coordinate: Coordinate)

    @Query("SELECT * FROM Coordinate")
    fun getAllCoordinates(): List<Coordinate>

    @Query("SELECT * FROM Coordinate WHERE id = :coordinateId")
    fun getCoordinateById(coordinateId: Int): Coordinate

    // Other database operations as needed
}
