package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts

@Dao
interface FavoriteDonutsDAO {
    @Insert
    fun addFavoriteDonut(favoriteDonut: FavoriteDonuts)

    @Delete
    fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts)

    @Query("SELECT * FROM Donut INNER JOIN FavoriteDonuts ON Donut.id = FavoriteDonuts.donutId WHERE FavoriteDonuts.userId = :userId")
    fun getFavoriteDonutsByUser(userId: Int): List<Donut>

    // Other database operations as needed
}