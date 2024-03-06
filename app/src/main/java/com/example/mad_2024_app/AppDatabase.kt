package com.example.mad_2024_app

import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.ShopVisitHistory
import com.example.mad_2024_app.database.User

// Assuming User, Shop, etc., are your entities
@Database(entities = [User::class, Shop::class, Coordinate::class, Donut::class, Address::class, FavoriteDonuts::class, FavoriteShops::class,
                     ShopVisitHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val TAG = "LogoGPSDatabase"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mad_2024_app_database"
                ).build()
                INSTANCE = instance
                Log.d(TAG, "Database instance created")
                instance
            }
        }
    }
}
