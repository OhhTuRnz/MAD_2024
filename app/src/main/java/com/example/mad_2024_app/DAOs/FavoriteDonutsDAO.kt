package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDonutsDAO {
    @Upsert
    suspend fun upsert(favoriteDonut: FavoriteDonuts) : Long

    @Delete
    suspend fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts)

    @Query("DELETE FROM FavoriteDonuts WHERE userId = :userId AND donutId = :donutId")
    fun removeFavoriteDonutById(userId: Int, donutId: Int)

    @Query("SELECT * FROM Donut INNER JOIN FavoriteDonuts ON Donut.donutId = FavoriteDonuts.donutId WHERE FavoriteDonuts.userId = :userId")
    fun getFavoriteDonutsByUser(userId: Int): Flow<List<Donut>>

    // Other database operations as needed
}
