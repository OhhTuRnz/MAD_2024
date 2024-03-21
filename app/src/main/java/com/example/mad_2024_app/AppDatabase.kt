package com.example.mad_2024_app

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.TypeConverters
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(entities = [User::class, Shop::class, Coordinate::class, Donut::class, Address::class, FavoriteDonuts::class, FavoriteShops::class, ShopVisitHistory::class], version = 5)
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
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                Log.d(TAG, "Database instance created")
                instance
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun populateDatabase(context: Context) {
            GlobalScope.launch(Dispatchers.IO) {
                val database = getDatabase(context)
                val coordinateDao = database.coordinateDao()
                val addressDao = database.addressDao()
                val shopDao = database.shopDao()

                // Insert coordinate
                val coordinate1 =
                    Coordinate(latitude = 40.510972939666736, longitude = -3.696400583332486)
                val coordinateId1 = coordinateDao.upsert(coordinate1)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId1")

                // Check if coordinate insertion was successful
                if (coordinateId1 != -1L) {
                    // Insert address associated with the coordinate
                    val address = Address(
                        street = "C. del Monasterio de Oseira",
                        city = "Madrid",
                        country = "Spain",
                        number = 19,
                        zipCode = 28949,
                        coordinateId = coordinateId1.toInt()
                    )
                    val addressId = addressDao.upsert(address)
                    Log.d(TAG, "Inserted address with ID: $addressId")

                    // Check if address insertion was successful
                    if (addressId != -1L) {
                        // Insert shop associated with the address
                        val tresOlivosShop1 = Shop(
                            name = "Panaix Bakery Coffee",
                            description = "Service options\n" +
                                    "\n" +
                                    "Kerbside pickup\n" +
                                    "\n" +
                                    "No-contact delivery\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Dine-in\n" +
                                    "\n" +
                                    "Same-day delivery",
                            addressId = addressId.toInt(),
                            locationId = coordinateId1.toInt()
                        )
                        val shopId = shopDao.upsert(tresOlivosShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId")
                    }
                }

                val coordinate2 =
                    Coordinate(latitude = 40.50064546438078, longitude = -3.691342396218224)
                val coordinateId2 = coordinateDao.upsert(coordinate2)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId2")

                // Check if coordinate insertion was successful
                if (coordinateId2 != -1L) {
                    // Insert address associated with the coordinate
                    val address2 = Address(
                        street = "Av. del Campo de Calatrava",
                        city = "Madrid",
                        country = "Spain",
                        number = 17,
                        zipCode = 28034,
                        coordinateId = coordinateId2.toInt()
                    )
                    val addressId2 = addressDao.upsert(address2)
                    Log.d(TAG, "Inserted address with ID: $addressId2")

                    // Check if address insertion was successful
                    if (addressId2 != -1L) {
                        // Insert shop associated with the address
                        val montecarmeloShop1 = Shop(
                            name = "Cafetería Tres Olivos",
                            description = "Service options\n" +
                                    "\n" +
                                    "Outdoor seating\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Dine-in\n" +
                                    "\n" +
                                    "Delivery",
                            addressId = addressId2.toInt(),
                            locationId = coordinateId2.toInt()
                        )
                        val shopId2 = shopDao.upsert(montecarmeloShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId2")
                    }
                }

                val coordinate3 = Coordinate(latitude = 40.4172309597888, longitude = -3.6738480248533203)
                val coordinateId3 = coordinateDao.upsert(coordinate3)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId3")

                // Check if coordinate insertion was successful
                if (coordinateId3 != -1L) {
                    // Insert address associated with the coordinate
                    val address3 = Address(
                        street = "C/ de Fernán González",
                        city = "Madrid",
                        country = "Spain",
                        number = 68,
                        zipCode = 28009,
                        coordinateId = coordinateId3.toInt()
                    )
                    val addressId3 = addressDao.upsert(address3)
                    Log.d(TAG, "Inserted address with ID: $addressId3")

                    // Check if address insertion was successful
                    if (addressId3 != -1L) {
                        // Insert shop associated with the address
                        val retiroShop1 = Shop(
                            name = "Rousquillas Bakery",
                            description = "Service options\n" +
                                    "\n" +
                                    "Kerbside pickup\n" +
                                    "\n" +
                                    "No-contact delivery\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping\n" +
                                    "\n" +
                                    "Dine-in",
                            addressId = addressId3.toInt(),
                            locationId = coordinateId3.toInt()
                        )
                        val shopId3 = shopDao.upsert(retiroShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId3")
                    }
                }
            }
        }
    }
}
