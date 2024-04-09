package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import kotlinx.coroutines.launch

class FavoriteShopsViewModel(private val favoriteShopsRepository: FavoriteShopsRepository) : ViewModel() {

    private val TAG = "FavoriteShopsViewModel"

    // Live data for UI
    private val _favoriteShops = MutableLiveData<List<Shop>>()
    val favoriteShops: LiveData<List<Shop>> = _favoriteShops

    fun getFavoriteShopsByUser(uuid: String?) = viewModelScope.launch {
        favoriteShopsRepository.getFavoriteShopsByUser(uuid).collect { shops ->
            _favoriteShops.postValue(shops)
        }
        Log.d(TAG, "Retrieved favorite shops for user with id: $uuid")
    }

    fun upsertFavoriteShop(favoriteShop: FavoriteShops) = viewModelScope.launch {
        favoriteShopsRepository.upsertFavoriteShop(favoriteShop)
        Log.d(TAG, "Upserted favorite shop with id: ${favoriteShop.shopId}")
    }

    fun removeFavoriteShop(favoriteShop: FavoriteShops) = viewModelScope.launch {
        favoriteShopsRepository.removeFavoriteShop(favoriteShop)
    }

    fun removeFavoriteShopById(uuid: String?, shopId: Int) = viewModelScope.launch {
        favoriteShopsRepository.removeFavoriteShopById(uuid, shopId)
        Log.d(TAG, "Removed favorite shop with id: $shopId for user with id: $uuid")
    }

    fun isShopFavorite(uuid: String?, shopId: Int): LiveData<Boolean> {
        val isFavorite = MutableLiveData<Boolean>()
        viewModelScope.launch {
            isFavorite.value = favoriteShopsRepository.isShopFavorite(uuid, shopId)
        }
        return isFavorite
    }
}