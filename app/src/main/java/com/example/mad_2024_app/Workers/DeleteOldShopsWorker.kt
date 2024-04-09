package com.example.mad_2024_app.Workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mad_2024_app.RepositoryProvider
import java.util.concurrent.TimeUnit

class DeleteOldShopsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "DeleteOldShopsWorker"
    private val shopRepo = RepositoryProvider.getShopRepository()

    override suspend fun doWork(): Result {
        return try {

            // Call the method in the repository to delete old shops
            shopRepo.deleteOldShops()

            Log.d(TAG, "Successfully deleted old shops.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting old shops", e)
            Result.retry()
        }
    }
}
