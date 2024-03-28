package com.example.mad_2024_app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mad_2024_app.database.User
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    private var activityCount = 0
    private val TAG = "AppActivity"
    lateinit var database: AppDatabase
        private set
    lateinit var userRepo : UserRepository
        private set
    lateinit var shopRepo : ShopRepository
        private set

    val cache: Cache<String, Any> by lazy {
        CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build()
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

        // Instantiate Repos
        userRepo = UserRepository(userDao, cache)
        shopRepo = ShopRepository(shopDao, cache)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Increment the activity count when a new activity is created
                activityCount++

                // Display greeting toast when the first activity is created
                if (activityCount == 1) {
                    val sharedPreferences = activity.getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
                    var username = sharedPreferences.getString("username", "User")
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
    }
}
