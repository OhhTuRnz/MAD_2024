package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.Coordinate
import kotlinx.coroutines.flow.Flow

@Dao
interface CoordinateDAO {
    @Upsert
    suspend fun upsert(coordinate: Coordinate) : Long

    @Query("SELECT * FROM Coordinate")
    fun getAllCoordinates(): Flow<List<Coordinate>>

    @Query("SELECT * FROM Coordinate WHERE coordinateId = :coordinateId")
    fun getCoordinateById(coordinateId: Int): Flow<Coordinate>

    @Delete
    suspend fun delete(coordinate: Coordinate)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Coordinate WHERE coordinateId = :coordinateId")
    fun deleteById(coordinateId: Int)
}
