package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.ShopRepository
import kotlinx.coroutines.launch

class ShopViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _shopsNearCoordinates = MediatorLiveData<List<Shop>?>()
    private val TAG = "ShopViewModel"

    val shopsNearCoordinates: LiveData<List<Shop>?> = _shopsNearCoordinates

    // Function to insert a shop
    fun insertShop(shop: Shop) = viewModelScope.launch {
        shopRepository.insert(shop)
    }

    // Function to get all shops near a set of coordinates
    fun getAllShopsNearCoordinates(location: Coordinate, radius: Int) = viewModelScope.launch {
        val liveData = shopRepository.getAllShopsNearCoordinates(location, radius)
        _shopsNearCoordinates.addSource(liveData) { shops ->
            Log.d(TAG, "Retrieving nearby shops, list size: ${_shopsNearCoordinates.value?.size}")
            _shopsNearCoordinates.value = shops
        }
    }

    /*
    fun getAllShops() = viewModelScope.launch {
        val liveData = shopRepository.getAllShops()
        _shopsNearCoordinates.addSource(liveData) { userData ->
            _shopsNearCoordinates.value = userData
        }
    }
     */
}