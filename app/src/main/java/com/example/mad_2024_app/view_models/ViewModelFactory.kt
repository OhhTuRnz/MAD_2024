package com.example.mad_2024_app.view_models

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.IRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.repositories.UserRepository

class ViewModelFactory(
    private val repository : IRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(repository as UserRepository) as T
            }

            modelClass.isAssignableFrom(ShopViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return ShopViewModel(repository as ShopRepository) as T
            }

            modelClass.isAssignableFrom(AddressViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return AddressViewModel(repository as AddressRepository) as T
            }

            modelClass.isAssignableFrom(CoordinateViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return CoordinateViewModel(repository as CoordinateRepository) as T
            }

            modelClass.isAssignableFrom(FavoriteShopsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return FavoriteShopsViewModel(repository as FavoriteShopsRepository) as T
            }

            modelClass.isAssignableFrom(FavoriteDonutsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return FavoriteDonutsViewModel(repository as FavoriteDonutsRepository) as T
            }

            modelClass.isAssignableFrom(ShopVisitHistoryViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return ShopVisitHistoryViewModel(repository as ShopVisitHistoryRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}