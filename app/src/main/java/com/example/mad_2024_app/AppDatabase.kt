package com.example.mad_2024_app

import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.test.core.app.ApplicationProvider
import com.example.mad_2024_app.DAOs.AddressDAO
import com.example.mad_2024_app.DAOs.CoordinateDAO
import com.example.mad_2024_app.DAOs.DonutDAO
import com.example.mad_2024_app.DAOs.FavoriteDonutsDAO
import com.example.mad_2024_app.DAOs.FavoriteShopsDAO
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.DAOs.ShopVisitHistoryDAO
import com.example.mad_2024_app.DAOs.UserDAO
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Converters
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.ShopVisitHistory
import com.example.mad_2024_app.database.User
import com.google.gson.Gson

@Database(entities = [User::class, Shop::class, Coordinate::class, Donut::class, Address::class, FavoriteDonuts::class, FavoriteShops::class, ShopVisitHistory::class], version = 1)
@TypeConverters(Converters::class) // Correct placement of annotation
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
    abstract fun shopDao(): ShopDAO
    abstract fun donutDao(): DonutDAO
    abstract fun addressDao(): AddressDAO
    abstract fun coordinateDao(): CoordinateDAO
    abstract fun favoriteDonutsDao(): FavoriteDonutsDAO
    abstract fun favoriteShopsDao(): FavoriteShopsDAO
    abstract fun shopVisitHistoryDao(): ShopVisitHistoryDAO
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val TAG = "LogoGPSDatabase"

        private lateinit var database: AppDatabase

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
