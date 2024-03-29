package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.User
import com.example.mad_2024_app.repositories.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ShopViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _shopsNearCoordinates = MutableLiveData<List<Shop>?>()
    val shopsNearCoordinates: LiveData<List<Shop>?> = _shopsNearCoordinates

    val addressIds = MutableLiveData<List<Int>>()

    private val TAG = "ShopViewModel"

    // Function to insert a shop
    fun upsertShop(shop: Shop) = viewModelScope.launch {
        shopRepository.upsert(shop)
    }

    // Function to get all shops near a set of coordinates
    fun getAllShopsNearCoordinates(location: Coordinate, radius: Int) = viewModelScope.launch {
        shopRepository.getAllShopsNearCoordinates(location, radius).collect { shops ->
            Log.d(TAG, "Retrieving nearby shops, list size: ${shops.size}")
            _shopsNearCoordinates.postValue(shops)

            // Update the address for updating the address view model
            val ids = shops.mapNotNull { it.addressId }
            addressIds.postValue(ids)
        }
    }

    suspend fun getShopByIdPreCollect(shopId: Int) : Flow<Shop?> {
        return shopRepository.getShopById(shopId)
    }
}