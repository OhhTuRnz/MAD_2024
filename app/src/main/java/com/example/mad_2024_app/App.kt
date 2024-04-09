package com.example.mad_2024_app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
<<<<<<< HEAD
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.User
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
=======
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mad_2024_app.Network.OverpassAPIService
import com.example.mad_2024_app.Workers.DeleteOldShopsWorker
import com.example.mad_2024_app.Workers.FetchDonutShopsWorker
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.DonutRepository
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.Network.OverpassAPIService
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
<<<<<<< HEAD
=======
import okhttp3.OkHttpClient
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class App : Application() {
    private var activityCount = 0

    private val TAG = "Application"

    lateinit var database: AppDatabase
        private set

    lateinit var userRepo : UserRepository
        private set

    lateinit var shopRepo : ShopRepository
        private set
    lateinit var addressRepo : AddressRepository
        private set
    lateinit var coordinateRepo : CoordinateRepository
        private set
    lateinit var favoriteShopsRepo : FavoriteShopsRepository
        private set
    lateinit var favoriteDonutsRepository: FavoriteDonutsRepository
        private set
    lateinit var shopVisitHistoryRepository: ShopVisitHistoryRepository
        private set

    lateinit var addressRepo : AddressRepository
        private set

    lateinit var coordinateRepo : CoordinateRepository
        private set

    lateinit var favoriteShopsRepo : FavoriteShopsRepository
        private set

    lateinit var favoriteDonutsRepo: FavoriteDonutsRepository
        private set

    lateinit var shopVisitHistoryRepo: ShopVisitHistoryRepository
        private set

    lateinit var donutRepo : DonutRepository
        private set

    val cache: Cache<String, Any> by lazy {
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build()
    }

<<<<<<< HEAD
    val retrofitOverpass = Retrofit.Builder()
=======
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
        .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
        .build()

    val retrofitOverpass = Retrofit.Builder()
        .client(okHttpClient)
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
        .baseUrl("https://overpass-api.de/api/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val overpassApi = retrofitOverpass.create(OverpassAPIService::class.java)

    companion object {
<<<<<<< HEAD
        fun getRetrofit(context: Context): Retrofit {
            return (context.applicationContext as App).retrofitOverpass
        }
    }

=======
        fun getOverpassRetrofit(context: Context): Retrofit {
            return (context.applicationContext as App).retrofitOverpass
        }
    }
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        Log.d(TAG, "onCreate: Database instance retrieved")

        // Launch a coroutine in the IO context to populate the database
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.populateDatabase(this@App)
        }

        // Instantiate DAOs
        val userDao = database.userDao()
        val shopDao = database.shopDao()
        val addressDao = database.addressDao()
        val coordinateDao = database.coordinateDao()
<<<<<<< HEAD
=======
        val donutDao = database.donutDao()
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
        val favoriteShopsDao = database.favoriteShopsDao()
        val favoriteDonutsDao = database.favoriteDonutsDao()
        val shopVisitHistoryDao = database.shopVisitHistoryDao()

        // Instantiate Repos
        userRepo = UserRepository(userDao, cache)
        shopRepo = ShopRepository(shopDao, cache)
        addressRepo = AddressRepository(addressDao, cache)
        coordinateRepo = CoordinateRepository(coordinateDao, cache)
<<<<<<< HEAD
        favoriteShopsRepo = FavoriteShopsRepository(favoriteShopsDao, cache)
        favoriteDonutsRepository = FavoriteDonutsRepository(favoriteDonutsDao, cache)
        shopVisitHistoryRepository = ShopVisitHistoryRepository(shopVisitHistoryDao, cache)
=======
        donutRepo = DonutRepository(donutDao, cache)
        favoriteShopsRepo = FavoriteShopsRepository(favoriteShopsDao, cache)
        favoriteDonutsRepo = FavoriteDonutsRepository(favoriteDonutsDao, cache)
        shopVisitHistoryRepo = ShopVisitHistoryRepository(shopVisitHistoryDao, cache)

        RepositoryProvider.initialize(
            userRepo,
            shopRepo,
            addressRepo,
            coordinateRepo,
            donutRepo,
            favoriteShopsRepo,
            favoriteDonutsRepo,
            shopVisitHistoryRepo
        )
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Increment the activity count when a new activity is created
                activityCount++

                // Display greeting toast when the first activity is created
                if (activityCount == 1) {
                    val sharedPreferences = activity.getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username", "User")
                    Toast.makeText(activity, "Hello, $username!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                // Decrement the activity count when an activity is destroyed
                activityCount--
            }
        })

<<<<<<< HEAD
        /*
        val workRequest = PeriodicWorkRequestBuilder<FetchStoresWorker>(1, TimeUnit.HOURS)
                // Additional constraints like network type can be specified here
                .build()

        WorkManager.getInstance(context).enqueue(workRequest)
         */
=======
        // Define constraints
        val constraintsFetchShops = Constraints.Builder()
            .setRequiresBatteryNotLow(true)  // Don't run if the battery is low
            .setRequiredNetworkType(NetworkType.CONNECTED) // Need to be connected
            .build()

        // Build your PeriodicWorkRequest with the constraints
        val fetchDonutShopsWorkRequest = PeriodicWorkRequestBuilder<FetchDonutShopsWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraintsFetchShops)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                5,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(this).cancelAllWork()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "fetchDonutShops",
            ExistingPeriodicWorkPolicy.KEEP,  // or REPLACE, depending on your needs
            fetchDonutShopsWorkRequest
        )

        // Define constraints
        val constraintsOldShopsDeleter = Constraints.Builder()
            .setRequiresBatteryNotLow(true)  // Don't run if the battery is low
            .setRequiredNetworkType(NetworkType.CONNECTED) // Need to be connected
            .build()

        val deleteOldShopsWorkRequest = PeriodicWorkRequestBuilder<DeleteOldShopsWorker>(30, TimeUnit.DAYS)
            .setConstraints(constraintsOldShopsDeleter)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                5,
                TimeUnit.MILLISECONDS
            )
            .build()

// Enqueue the work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deleteOldShopsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            deleteOldShopsWorkRequest
        )
>>>>>>> c13d8285fee9e4c00aa3cc5b52ccd5eae0bfa09a
    }
}