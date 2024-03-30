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

@Database(
    entities = [User::class, Shop::class, Coordinate::class, Donut::class, Address::class,
        FavoriteDonuts::class, FavoriteShops::class, ShopVisitHistory::class], version = 10
)
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
                val donutDAO = database.donutDao()

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
                            locationId = coordinateId1.toInt(),
                            lastAccessed = System.currentTimeMillis()
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
                            locationId = coordinateId2.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId2 = shopDao.upsert(montecarmeloShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId2")
                    }
                }

                val coordinate3 =
                    Coordinate(latitude = 40.4172309597888, longitude = -3.6738480248533203)
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
                            locationId = coordinateId3.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId3 = shopDao.upsert(retiroShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId3")
                    }
                }

                val coordinate4 =
                    Coordinate(latitude = 40.30107009651909, longitude = -3.4422432300331773)
                val coordinateId4 = coordinateDao.upsert(coordinate4)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId4")

                // Check if coordinate insertion was successful
                if (coordinateId4 != -1L) {
                    // Insert address associated with the coordinate
                    val address4 = Address(
                        street = "Av. del Ejército",
                        city = "Arganda del Rey, Madrid",
                        country = "Spain",
                        number = 1,
                        zipCode = 28500,
                        coordinateId = coordinateId4.toInt()
                    )
                    val addressId4 = addressDao.upsert(address4)
                    Log.d(TAG, "Inserted address with ID: $addressId4")

                    // Check if address insertion was successful
                    if (addressId4 != -1L) {
                        // Insert shop associated with the address
                        val argandaShop1 = Shop(
                            name = "Granier",
                            description = "Service options\n" +
                                    "\n" +
                                    "Same-day delivery\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping\n" +
                                    "\n" +
                                    "Dine-in",
                            addressId = addressId4.toInt(),
                            locationId = coordinateId4.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId4 = shopDao.upsert(argandaShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId4")
                    }
                }

                val coordinate5 =
                    Coordinate(latitude = 40.30067491940748, longitude = -3.43802363634576)
                val coordinateId5 = coordinateDao.upsert(coordinate5)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId5")

                // Check if coordinate insertion was successful
                if (coordinateId5 != -1L) {
                    // Insert address associated with the coordinate
                    val address5 = Address(
                        street = "Pl. de la Constitución",
                        city = "Arganda del Rey, Madrid",
                        country = "Spain",
                        number = 14,
                        zipCode = 28500,
                        coordinateId = coordinateId5.toInt()
                    )
                    val addressId5 = addressDao.upsert(address5)
                    Log.d(TAG, "Inserted address with ID: $addressId5")

                    // Check if address insertion was successful
                    if (addressId5 != -1L) {
                        // Insert shop associated with the address
                        val plazaShop1 = Shop(
                            name = "Sweet Sensations",
                            description = "Service options\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping\n" +
                                    "\n" +
                                    "Dine-in",
                            addressId = addressId5.toInt(),
                            locationId = coordinateId5.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId5 = shopDao.upsert(plazaShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId5")
                    }
                }

                val coordinate6 =
                    Coordinate(latitude = 40.307904197813315, longitude = -3.4514675126743284)
                val coordinateId6 = coordinateDao.upsert(coordinate6)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId6")

                // Check if coordinate insertion was successful
                if (coordinateId6 != -1L) {
                    // Insert address associated with the coordinate
                    val address6 = Address(
                        street = "C. San Sebastián",
                        city = "Arganda del Rey, Madrid",
                        country = "Spain",
                        number = 29,
                        zipCode = 28500,
                        coordinateId = coordinateId6.toInt()
                    )
                    val addressId6 = addressDao.upsert(address6)
                    Log.d(TAG, "Inserted address with ID: $addressId6")

                    // Check if address insertion was successful
                    if (addressId6 != -1L) {
                        // Insert shop associated with the address
                        val palmerasShop1 = Shop(
                            name = "Panadería las Palmeras",
                            description = "Service options\n" +
                                    "\n" +
                                    "Same-day delivery\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId6.toInt(),
                            locationId = coordinateId6.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId6 = shopDao.upsert(palmerasShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId6")
                    }
                }

                val coordinate7 =
                    Coordinate(latitude = 40.386265118568275, longitude = -3.626206066547623)
                val coordinateId7 = coordinateDao.upsert(coordinate7)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId7")

                // Check if coordinate insertion was successful
                if (coordinateId7 != -1L) {
                    // Insert address associated with the coordinate
                    val address7 = Address(
                        street = "C. de Arboleda",
                        city = "Puente de Vallecas, Madrid",
                        country = "Spain",
                        number = 18,
                        zipCode = 28031,
                        coordinateId = coordinateId7.toInt()
                    )
                    val addressId7 = addressDao.upsert(address7)
                    Log.d(TAG, "Inserted address with ID: $addressId7")

                    // Check if address insertion was successful
                    if (addressId7 != -1L) {
                        // Insert shop associated with the address
                        val rosaShop1 = Shop(
                            name = "Chef Rosa - Sanuts Madrid",
                            description = "Service options\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "No-contact delivery\n" +
                                    "\n" +
                                    "Kerbside pickup",
                            addressId = addressId7.toInt(),
                            locationId = coordinateId7.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId7 = shopDao.upsert(rosaShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId7")
                    }
                }

                val coordinate8 =
                    Coordinate(latitude = 40.387030121832545, longitude = -3.6369144918894207)
                val coordinateId8 = coordinateDao.upsert(coordinate8)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId8")

                // Check if coordinate insertion was successful
                if (coordinateId8 != -1L) {
                    // Insert address associated with the coordinate
                    val address8 = Address(
                        street = "C/ de León Felipe",
                        city = "Puente de Vallecas, Madrid",
                        country = "Spain",
                        number = 14,
                        zipCode = 28038,
                        coordinateId = coordinateId8.toInt()
                    )
                    val addressId8 = addressDao.upsert(address8)
                    Log.d(TAG, "Inserted address with ID: $addressId8")

                    // Check if address insertion was successful
                    if (addressId8 != -1L) {
                        // Insert shop associated with the address
                        val familiarShop1 = Shop(
                            name = "Pan Familiar",
                            description = "Service options\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "Same-day Delivery\n" +
                                    "\n" +
                                    "In-store shopping\n" +
                                    "\n" +
                                    "Dine-in",
                            addressId = addressId8.toInt(),
                            locationId = coordinateId8.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId8 = shopDao.upsert(familiarShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId8")
                    }
                }

                val coordinate9 =
                    Coordinate(latitude = 40.383055202359586, longitude = -3.6258477233808137)
                val coordinateId9 = coordinateDao.upsert(coordinate9)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId9")

                // Check if coordinate insertion was successful
                if (coordinateId9 != -1L) {
                    // Insert address associated with the coordinate
                    val address9 = Address(
                        street = "Av. de la Democracia",
                        city = "Puente de Vallecas, Madrid",
                        country = "Spain",
                        number = 3,
                        zipCode = 28031,
                        coordinateId = coordinateId9.toInt()
                    )
                    val addressId9 = addressDao.upsert(address9)
                    Log.d(TAG, "Inserted address with ID: $addressId9")

                    // Check if address insertion was successful
                    if (addressId9 != -1L) {
                        // Insert shop associated with the address
                        val mercadonaShop1 = Shop(
                            name = "Mercadona",
                            description = "Service options\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId9.toInt(),
                            locationId = coordinateId9.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId9 = shopDao.upsert(mercadonaShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId9")
                    }
                }

                val coordinate10 =
                    Coordinate(latitude = 40.38304184920588, longitude = -3.6280389747858615)
                val coordinateId10 = coordinateDao.upsert(coordinate10)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId10")

                // Check if coordinate insertion was successful
                if (coordinateId10 != -1L) {
                    // Insert address associated with the coordinate
                    val address10 = Address(
                        street = "C. de Bruno Abúndez",
                        city = "Puente de Vallecas, Madrid",
                        country = "Spain",
                        number = 54,
                        zipCode = 28031,
                        coordinateId = coordinateId10.toInt()
                    )
                    val addressId10 = addressDao.upsert(address10)
                    Log.d(TAG, "Inserted address with ID: $addressId10")

                    // Check if address insertion was successful
                    if (addressId10 != -1L) {
                        // Insert shop associated with the address
                        val supecoShop1 = Shop(
                            name = "Supeco",
                            description = "Service options\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "Same-day delivery\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId10.toInt(),
                            locationId = coordinateId10.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId10 = shopDao.upsert(supecoShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId10")
                    }
                }

                val coordinate11 =
                    Coordinate(latitude = 40.38672980040134, longitude = -3.638607988974512)
                val coordinateId11 = coordinateDao.upsert(coordinate11)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId11")

                // Check if coordinate insertion was successful
                if (coordinateId11 != -1L) {
                    // Insert address associated with the coordinate
                    val address11 = Address(
                        street = "Av. de la Albufera",
                        city = "Puente de Vallecas, Madrid",
                        country = "Spain",
                        number = 300,
                        zipCode = 28018,
                        coordinateId = coordinateId11.toInt()
                    )
                    val addressId11 = addressDao.upsert(address11)
                    Log.d(TAG, "Inserted address with ID: $addressId11")

                    // Check if address insertion was successful
                    if (addressId11 != -1L) {
                        // Insert shop associated with the address
                        val robertsShop1 = Shop(
                            name = "" +
                                    "T'S",
                            description = "Service options\n" +
                                    "\n" +
                                    "Same-day delivery\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "Takeaway\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId11.toInt(),
                            locationId = coordinateId11.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId11 = shopDao.upsert(robertsShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId11")
                    }
                }

                val coordinate12 =
                    Coordinate(latitude = 40.50148291838675, longitude = -3.6906254736554396)
                val coordinateId12 = coordinateDao.upsert(coordinate12)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId12")

                // Check if coordinate insertion was successful
                if (coordinateId12 != -1L) {
                    // Insert address associated with the coordinate
                    val address12 = Address(
                        street = "Pl. Tres Olivos",
                        city = "Fuencarral-El Pardo, Madrid",
                        country = "Spain",
                        number = 2,
                        zipCode = 28034,
                        coordinateId = coordinateId12.toInt()
                    )
                    val addressId12 = addressDao.upsert(address12)
                    Log.d(TAG, "Inserted address with ID: $addressId12")

                    // Check if address insertion was successful
                    if (addressId12 != -1L) {
                        // Insert shop associated with the address
                        val briocherieShop1 = Shop(
                            name = "Pastelería La Briocherie",
                            description = "Service options\n" +
                                    "\n" +
                                    "Same-day delivery\n" +
                                    "\n" +
                                    "Delivery\n" +
                                    "\n" +
                                    "Dine-in\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId12.toInt(),
                            locationId = coordinateId12.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId12 = shopDao.upsert(briocherieShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId12")
                    }
                }

                val coordinate13 =
                    Coordinate(latitude = 40.50710527518719, longitude = -3.6948296648973167)
                val coordinateId13 = coordinateDao.upsert(coordinate13)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId13")

                // Check if coordinate insertion was successful
                if (coordinateId13 != -1L) {
                    // Insert address associated with the coordinate
                    val address13 = Address(
                        street = "C. del Monasterio de Liébana",
                        city = "Fuencarral-El Pardo, Madrid",
                        country = "Spain",
                        number = 5,
                        zipCode = 28049,
                        coordinateId = coordinateId13.toInt()
                    )
                    val addressId13 = addressDao.upsert(address13)
                    Log.d(TAG, "Inserted address with ID: $addressId13")

                    // Check if address insertion was successful
                    if (addressId13 != -1L) {
                        // Insert shop associated with the address
                        val bergueShop1 = Shop(
                            name = "Bergue Montecarmelo",
                            description = "Service options\n" +
                                    "\n" +
                                    "In-store pick-up\n" +
                                    "\n" +
                                    "In-store shopping",
                            addressId = addressId13.toInt(),
                            locationId = coordinateId13.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId13 = shopDao.upsert(bergueShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId13")
                    }
                }

                val coordinate14 =
                    Coordinate(latitude = 40.50353974997118, longitude = -3.708478944806984)
                val coordinateId14 = coordinateDao.upsert(coordinate14)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId14")

                // Check if coordinate insertion was successful
                if (coordinateId14 != -1L) {
                    // Insert address associated with the coordinate
                    val address14 = Address(
                        street = "Av. del Monasterio de Silos",
                        city = "Fuencarral-El Pardo, Madrid",
                        country = "Spain",
                        number = 20,
                        zipCode = 28034,
                        coordinateId = coordinateId14.toInt()
                    )
                    val addressId14 = addressDao.upsert(address14)
                    Log.d(TAG, "Inserted address with ID: $addressId14")

                    // Check if address insertion was successful
                    if (addressId14 != -1L) {
                        // Insert shop associated with the address
                        val panariaShop1 = Shop(
                            name = "PANARIA MONTECARMELO",
                            description = "Service options\n" +
                                    "\n" +
                                    "Outdoor seating\n" +
                                    "\n" +
                                    "No-contact delivery\n" +
                                    "\n" +
                                    "Dine-in\n" +
                                    "\n" +
                                    "Kerbside pickup\n\n" +
                                    "\n" +
                                    "Takeaway",
                            addressId = addressId14.toInt(),
                            locationId = coordinateId14.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId14 = shopDao.upsert(panariaShop1)
                        Log.d(TAG, "Inserted shop with ID: $shopId14")
                    }
                }

                val coordinate15 =
                    Coordinate(latitude = 40.72514755869697, longitude = -2.483507878536185)
                val coordinateId15 = coordinateDao.upsert(coordinate15)
                Log.d(TAG, "Inserted coordinate with ID: $coordinateId15")

                // Check if coordinate insertion was successful
                if (coordinateId15 != -1L) {
                    // Insert address associated with the coordinate
                    val address15 = Address(
                        street = "C. Plazuela",
                        city = "Cifuentes, Guadalajara",
                        country = "Spain",
                        number = 4,
                        zipCode = 19431,
                        coordinateId = coordinateId15.toInt()
                    )
                    val addressId15 = addressDao.upsert(address15)
                    Log.d(TAG, "Inserted address with ID: $addressId15")

                    // Check if address insertion was successful
                    if (addressId15 != -1L) {
                        // Insert shop associated with the address
                        val carrascosaShop = Shop(
                            name = "Bar Salon",
                            description = "Service options\n" +
                                    "\n" +
                                    "Dine-in\n" +
                                    "Offerings\n" +
                                    "\n" +
                                    "Alcohol\n" +
                                    "\n" +
                                    "Beer\n" +
                                    "\n" +
                                    "Food\n" +
                                    "\n" +
                                    "Spirits\n" +
                                    "\n" +
                                    "Wine",
                            addressId = addressId15.toInt(),
                            locationId = coordinateId15.toInt(),
                            lastAccessed = System.currentTimeMillis()
                        )
                        val shopId15 = shopDao.upsert(carrascosaShop)
                        Log.d(TAG, "Inserted shop with ID: $shopId15")
                    }
                    val coordinate16 =
                        Coordinate(latitude = 36.71210457229509, longitude = -4.431699546343177)
                    val coordinateId16 = coordinateDao.upsert(coordinate16)
                    Log.d(TAG, "Inserted coordinate with ID: $coordinateId16")

                    // Check if coordinate insertion was successful
                    if (coordinateId16 != -1L) {
                        // Insert address associated with the coordinate
                        val address16 = Address(
                            street = "C. Plazuela",
                            city = "Cifuentes, Guadalajara",
                            country = "Spain",
                            number = 4,
                            zipCode = 19431,
                            coordinateId = coordinateId16.toInt()
                        )
                        val addressId16 = addressDao.upsert(address16)
                        Log.d(TAG, "Inserted address with ID: $addressId16")

                        // Check if address insertion was successful
                        if (addressId16 != -1L) {
                            // Insert shop associated with the address
                            val dunkinZambrano = Shop(
                                name = "DUNKIN´ESPAÑA Zambrano",
                                description = "Service options\n" +
                                        "\n" +
                                        "Delivery\n" +
                                        "\n" +
                                        "Takeaway\n" +
                                        "\n" +
                                        "Dine-in\n" +
                                        "Accessibility\n" +
                                        "\n" +
                                        "Wheelchair-accessible entrance\n" +
                                        "\n" +
                                        "Wheelchair-accessible toilet\n" +
                                        "Offerings\n" +
                                        "\n" +
                                        "Coffee\n" +
                                        "Dining options\n" +
                                        "\n" +
                                        "Breakfast\n" +
                                        "\n" +
                                        "Brunch\n" +
                                        "\n" +
                                        "Dessert\n" +
                                        "Amenities\n" +
                                        "\n" +
                                        "Bar on site\n" +
                                        "\n" +
                                        "Toilets\n" +
                                        "Atmosphere\n" +
                                        "\n" +
                                        "Casual\n" +
                                        "Planning\n" +
                                        "\n" +
                                        "Accepts reservations\n" +
                                        "Payments\n" +
                                        "\n" +
                                        "Credit cards\n" +
                                        "\n" +
                                        "Debit cards\n" +
                                        "Children\n" +
                                        "\n" +
                                        "Good for kids",
                                addressId = addressId16.toInt(),
                                locationId = coordinateId16.toInt(),
                                lastAccessed = System.currentTimeMillis()
                            )
                            val shopId16 = shopDao.upsert(dunkinZambrano)
                            Log.d(TAG, "Inserted shop with ID: $shopId16")
                        }
                    }
                }

                val donutsList = listOf(
                    Donut(name = "Glazed Donut", type = "Classic", color = "Golden"),
                    Donut(name = "Chocolate Donut", type = "Chocolate", color = "Brown"),
                    Donut(name = "Jelly Donut", type = "Filled", color = "Assorted"),
                    Donut(name = "Boston Cream Donut", type = "Filled", color = "Chocolate"),
                    Donut(name = "Maple Bar Donut", type = "Bar", color = "Golden"),
                    Donut(name = "Old Fashioned Donut", type = "Classic", color = "Golden"),
                    Donut(name = "Blueberry Donut", type = "Fruit", color = "Blue"),
                    Donut(name = "Vanilla Cream Donut", type = "Filled", color = "White"),
                    Donut(name = "Strawberry Donut", type = "Fruit", color = "Red"),
                    Donut(name = "Cinnamon Sugar Donut", type = "Classic", color = "Brown"),
                    Donut(name = "Lemon Donut", type = "Citrus", color = "Yellow"),
                    Donut(name = "Raspberry Donut", type = "Fruit", color = "Pink"),
                    Donut(name = "Coconut Donut", type = "Coconut", color = "White"),
                    Donut(name = "Red Velvet Donut", type = "Red Velvet", color = "Red"),
                    Donut(name = "Pistachio Donut", type = "Nut", color = "Green"),
                    Donut(name = "Powdered Sugar Donut", type = "Classic", color = "White"),
                    Donut(name = "Caramel Donut", type = "Filled", color = "Brown"),
                    Donut(name = "Apple Fritter Donut", type = "Fritter", color = "Assorted"),
                    Donut(name = "Peanut Butter Donut", type = "Nut", color = "Brown")
                )

                for (donut in donutsList) {
                    // Insertamos cada Donut en la base de datos utilizando el método upsert del DAO
                    val donutId = donutDAO.upsert(donut)
                    Log.d(TAG, "Inserted donut '${donut.name}' with ID: $donutId")
                }
            }
        }
    }
}
