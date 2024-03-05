package com.example.mad_2024_app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast

class App : Application() {
    private var activityCount = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Increment the activity count when a new activity is created
                activityCount++

                // Display greeting toast when the first activity is created
                if (activityCount == 1) {
                    val sharedPreferences = activity.getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("username", "")
                    Toast.makeText(activity, "Hello, $username!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                // Decrement the activity count when an activity is stopped
                activityCount--
                // If no activity is active, the app is going to background
                if (activityCount == 0) {
                    // Set isFirstRun to false when app goes to background
                    val sharedPreferences = activity.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                // Decrement the activity count when an activity is destroyed
                activityCount--
            }
        })
    }
}
