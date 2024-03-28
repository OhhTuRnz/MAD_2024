package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.ShopVisitHistory
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShopVisitHistoryViewModel(private val shopVisitHistoryRepository: ShopVisitHistoryRepository) : ViewModel() {

    private val _visitHistory = MutableLiveData<List<ShopVisitHistory>?>()
    val visitHistory: LiveData<List<ShopVisitHistory>?> = _visitHistory
    private val TAG = "ShopVisitHistoryViewModel"

    fun getVisitsByUser(uuid: String) = viewModelScope.launch(Dispatchers.IO) {
        shopVisitHistoryRepository.getVisitsByUser(uuid).collect { visits ->
            Log.d(TAG, "Retrieving visits for UUID: $uuid")
            _visitHistory.value = visits
        }
    }

    fun upsert(shopVisit: ShopVisitHistory) = viewModelScope.launch(Dispatchers.IO) {
        shopVisitHistoryRepository.upsert(shopVisit)
    }

    fun removeVisit(shopVisit: ShopVisitHistory) = viewModelScope.launch(Dispatchers.IO) {
        shopVisitHistoryRepository.removeVisit(shopVisit)
    }

    fun removeVisitById(visitorUuid: String, visitedShopId : Int, timestamp: Long) = viewModelScope.launch(Dispatchers.IO) {
        shopVisitHistoryRepository.removeVisitById(visitorUuid, visitedShopId, timestamp)
    }

    fun removeUserVisitHistory(visitorUuid: String) = viewModelScope.launch(Dispatchers.IO) {
        shopVisitHistoryRepository.removeUserVisitHistory(visitorUuid)
    }

    // Additional methods as needed
}
