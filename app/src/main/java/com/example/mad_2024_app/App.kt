package com.example.mad_2024_app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mad_2024_app.Network.OverpassAPIService
import com.example.mad_2024_app.Workers.FetchDonutShopsWorker
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class App : Application() {
    private var activityCount = 0

    private val TAG = "Application"

    lateinit var database: AppDatabase

    lateinit var userRepo : UserRepository

    lateinit var shopRepo : ShopRepository

    lateinit var addressRepo : AddressRepository

    lateinit var coordinateRepo : CoordinateRepository

    lateinit var favoriteShopsRepo : FavoriteShopsRepository

    lateinit var favoriteDonutsRepo: FavoriteDonutsRepository

    lateinit var shopVisitHistoryRepo: ShopVisitHistoryRepository

    val cache: Cache<String, Any> by lazy {
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build()
    }

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
        .readTimeout(30, TimeUnit.SECONDS) // Increase read timeout
        .build()

    val retrofitOverpass = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://overpass-api.de/api/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val overpassApi = retrofitOverpass.create(OverpassAPIService::class.java)

    companion object {
        fun getOverpassRetrofit(context: Context): Retrofit {
            return (context.applicationContext as App).retrofitOverpass
        }
    }
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
        val favoriteShopsDao = database.favoriteShopsDao()
        val favoriteDonutsDao = database.favoriteDonutsDao()
        val shopVisitHistoryDao = database.shopVisitHistoryDao()

        // Instantiate Repos
        userRepo = UserRepository(userDao, cache)
        shopRepo = ShopRepository(shopDao, cache)
        addressRepo = AddressRepository(addressDao, cache)
        coordinateRepo = CoordinateRepository(coordinateDao, cache)
        favoriteShopsRepo = FavoriteShopsRepository(favoriteShopsDao, cache)
        favoriteDonutsRepo = FavoriteDonutsRepository(favoriteDonutsDao, cache)
        shopVisitHistoryRepo = ShopVisitHistoryRepository(shopVisitHistoryDao, cache)

        RepositoryProvider.initialize(
            userRepo,
            shopRepo,
            addressRepo,
            coordinateRepo,
            favoriteShopsRepo,
            favoriteDonutsRepo,
            shopVisitHistoryRepo
        )

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

        // Define constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)  // Don't run if the battery is low
            .setRequiredNetworkType(NetworkType.CONNECTED) // Need to be connected
            .build()

        val inputData = Data.Builder()
            .putString("key1", "value1") // Example parameter 1
            .putInt("key2", 123) // Example parameter 2
            .build()

        // Build your PeriodicWorkRequest with the constraints
        val fetchDonutShopsWorkRequest = PeriodicWorkRequestBuilder<FetchDonutShopsWorker>(5, TimeUnit.SECONDS)
            .setInputData(inputData)
            .setConstraints(constraints)
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
    }
}