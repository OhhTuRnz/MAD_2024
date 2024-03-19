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

    private val _shopsNearCoordinates = MutableLiveData<List<Shop>?>()
    val shopsNearCoordinates: LiveData<List<Shop>?> = _shopsNearCoordinates
    private val TAG = "ShopViewModel"

    // Function to insert a shop
    fun insertShop(shop: Shop) = viewModelScope.launch {
        shopRepository.insert(shop)
    }

    // Function to get all shops near a set of coordinates
    fun getAllShopsNearCoordinates(location: Coordinate, radius: Int) = viewModelScope.launch {
        shopRepository.getAllShopsNearCoordinates(location, radius).collect { shops ->
            Log.d(TAG, "Retrieving nearby shops, list size: ${shops.size}")
            _shopsNearCoordinates.postValue(shops)
        }
    }

    /*
    fun getAllShops() = viewModelScope.launch {
        shopRepository.getAllShops().collect { shops ->
            _shopsNearCoordinates.postValue(shops)
        }
    }
     */
}