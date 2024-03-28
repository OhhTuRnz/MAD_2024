package com.example.mad_2024_app.Controller

import android.content.Context
import android.content.SharedPreferences
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mad_2024_app.Activities.ILocationProvider
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.FavoriteShopsViewModel
import com.example.mad_2024_app.view_models.ShopViewModel

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val shopViewModel: ShopViewModel,
    private val favoriteShopsViewModel: FavoriteShopsViewModel,
    private val coordinateViewModel: CoordinateViewModel,
    private val sharedPreferences: SharedPreferences,
    private val addressViewModel: AddressViewModel,
    private val locationProvider: ILocationProvider,
    private val context: Context
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val NUM_PAGES = 2

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Main.newInstance(
                shopViewModel,
                favoriteShopsViewModel,
                coordinateViewModel, sharedPreferences, addressViewModel,
                locationProvider, context
            )
            1 -> Second()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}