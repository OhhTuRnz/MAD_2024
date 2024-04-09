package com.example.mad_2024_app.Workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FetchDonutShopsWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch location
            val (latitude, longitude) = getCurrentLocation()

            // Construct the Overpass API query
            val query = """
                [out:json];
                node
                  ["amenity"="cafe"]
                  ["cuisine"="donut"]
                  (around:1000, $latitude, $longitude);
                out;
            """

            // Access Retrofit for Overpass API
            val retrofit = App.getOverpassRetrofit(applicationContext).getOverpassRetrofit(applicationContext)
            val overpassApiService = retrofit.create(OverpassApiService::class.java)

            // Execute the API call
            val response = overpassApiService.queryOverpass(query).execute()

            if (response.isSuccessful) {
                val shopsData = response.body()  // Raw JSON String

                // Parse the JSON response to your data objects
                // Insert/update your database with this data
                // Example: shopRepository.insertShops(parsedShopsData)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }


    private fun getCurrentLocation(): Pair<Double, Double> {
        // Implement location fetching logic
        // Make sure to handle location permissions
        return Pair(0.0, 0.0)
    }
}
