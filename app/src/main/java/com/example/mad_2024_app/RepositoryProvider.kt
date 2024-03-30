package com.example.mad_2024_app

import android.app.Application
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.DonutRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.repositories.UserRepository

object RepositoryProvider {
    private lateinit var userRepo: UserRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository
    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var donutRepo : DonutRepository
    private lateinit var favoriteShopsRepo: FavoriteShopsRepository
    private lateinit var favoriteDonutsRepo: FavoriteDonutsRepository
    private lateinit var shopVisitHistoryRepo: ShopVisitHistoryRepository

    fun initialize(
        userRepo: UserRepository,
        shopRepo: ShopRepository,
        addressRepo: AddressRepository,
        coordinateRepo: CoordinateRepository,
        donutRepo: DonutRepository,
        favoriteShopsRepo: FavoriteShopsRepository,
        favoriteDonutsRepo: FavoriteDonutsRepository,
        shopVisitHistoryRepo: ShopVisitHistoryRepository,
        // Add other repositories here...
    ) {
        this.userRepo = userRepo
        this.shopRepo = shopRepo
        this.addressRepo = addressRepo
        this.coordinateRepo = coordinateRepo
        this.donutRepo = donutRepo
        this.favoriteShopsRepo = favoriteShopsRepo
        this.favoriteDonutsRepo = favoriteDonutsRepo
        this.shopVisitHistoryRepo = shopVisitHistoryRepo
        // Initialize other repositories here...
    }

    fun getUserRepository(): UserRepository {
        return userRepo
    }

    fun getShopRepository(): ShopRepository {
        return shopRepo
    }

    fun getAddressRepository(): AddressRepository {
        return addressRepo
    }

    fun getCoordinateRepository(): CoordinateRepository {
        return coordinateRepo
    }

    fun getFavoriteShopsRepository(): FavoriteShopsRepository {
        return favoriteShopsRepo
    }

    fun getFavoriteDonutsRepository(): FavoriteDonutsRepository {
        return favoriteDonutsRepo
    }

    fun getShopVisitHistoryRepository(): ShopVisitHistoryRepository {
        return shopVisitHistoryRepo
    }

    fun getDonutRepository(): DonutRepository {
        return donutRepo
    }
}