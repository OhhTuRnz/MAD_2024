package com.example.mad_2024_app.view_models

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
    }



    fun upsertFavoriteShop(favoriteShop: FavoriteShops) = viewModelScope.launch {
        favoriteShopsRepository.upsertFavoriteShop(favoriteShop)
    }

    fun removeFavoriteShop(favoriteShop: FavoriteShops) = viewModelScope.launch {
        favoriteShopsRepository.removeFavoriteShop(favoriteShop)
    }

    fun removeFavoriteShopById(uuid: String?, shopId: Int) = viewModelScope.launch {
        favoriteShopsRepository.removeFavoriteShopById(uuid, shopId)
    }

    fun isShopFavorite(uuid: String?, shopId: Int): LiveData<Boolean> {
        val isFavorite = MutableLiveData<Boolean>()
        viewModelScope.launch {
            isFavorite.value = favoriteShopsRepository.isShopFavorite(uuid, shopId)
        }
        return isFavorite
    }
}