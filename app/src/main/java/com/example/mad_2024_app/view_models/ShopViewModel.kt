package com.example.mad_2024_app.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.ShopRepository
import kotlinx.coroutines.launch

class ShopViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    // Function to insert a shop
    fun insertShop(shop: Shop) = viewModelScope.launch {
        shopRepository.insert(shop)
    }

    // Function to get all shops near a set of coordinates
    suspend fun getAllShopsNearCoordinates(location: Coordinate, radius: Int): LiveData<List<Shop>> {
        return shopRepository.getAllShopsNearCoordinates(location, radius)
    }
}