package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts

@Dao
interface FavoriteDonutsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFavoriteDonut(favoriteDonut: FavoriteDonuts) : Long

    @Delete
    fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts)

    @Query("DELETE FROM FavoriteDonuts WHERE userId = :userId AND donutId = :donutId")
    fun removeFavoriteDonutById(userId: Int, donutId: Int)

    @Query("SELECT * FROM Donut INNER JOIN FavoriteDonuts ON Donut.donutId = FavoriteDonuts.donutId WHERE FavoriteDonuts.userId = :userId")
    fun getFavoriteDonutsByUser(userId: Int): List<Donut>

    // Other database operations as needed
}
