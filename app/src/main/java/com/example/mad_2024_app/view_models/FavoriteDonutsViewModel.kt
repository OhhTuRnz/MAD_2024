package com.example.mad_2024_app.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.FavoriteDonuts
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import kotlinx.coroutines.launch

class FavoriteDonutsViewModel(private val favoriteDonutsRepository: FavoriteDonutsRepository) : ViewModel() {

    private val TAG = "FavoriteDonutsViewModel"

    // Live data for UI
    private val _favoriteDonuts = MutableLiveData<List<Donut>>()
    val favoriteDonuts: LiveData<List<Donut>> = _favoriteDonuts

    fun getFavoriteDonutsByUser(userId: String?) = viewModelScope.launch {
        favoriteDonutsRepository.getFavoriteDonutsByUser(userId).collect { donuts ->
            _favoriteDonuts.postValue(donuts)
        }
    }

    fun upsertFavoriteDonut(favoriteDonut: FavoriteDonuts) = viewModelScope.launch {
        favoriteDonutsRepository.upsertFavoriteDonut(favoriteDonut)
    }

    fun removeFavoriteDonut(favoriteDonut: FavoriteDonuts) = viewModelScope.launch {
        favoriteDonutsRepository.removeFavoriteDonut(favoriteDonut)
    }

    fun removeFavoriteDonutById(userId: Int, donutId: Int) = viewModelScope.launch {
        favoriteDonutsRepository.removeFavoriteDonutById(userId, donutId)
    }

    fun isDonutFavorite(userId: Int, donutId: Int): LiveData<Boolean> {
        val isFavorite = MutableLiveData<Boolean>()
        viewModelScope.launch {
            isFavorite.value = favoriteDonutsRepository.isDonutFavorite(userId, donutId)
        }
        return isFavorite
    }
}
