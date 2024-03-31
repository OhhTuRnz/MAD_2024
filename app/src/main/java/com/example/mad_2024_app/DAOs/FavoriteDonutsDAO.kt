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

    @Query("DELETE FROM FavoriteDonuts WHERE uuid = :uuid AND donutId = :donutId")
    fun removeFavoriteDonutById(uuid: String, donutId: Int)

    @Query("SELECT * FROM Donut INNER JOIN FavoriteDonuts ON Donut.donutId = FavoriteDonuts.donutId WHERE FavoriteDonuts.uuid = :uuid")
    fun getFavoriteDonutsByUser(uuid: String): Flow<List<Donut>>

    @Query("SELECT EXISTS(SELECT 1 FROM FavoriteDonuts WHERE uuid = :uuid AND donutId = :donutId)")
    fun isFavorite(uuid: String, donutId: Int): Boolean
}
