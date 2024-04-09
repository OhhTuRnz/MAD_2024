package com.example.mad_2024_app.Controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.Activities.ILocationProvider
import com.example.mad_2024_app.Activities.MainActivity
import com.example.mad_2024_app.Activities.OpenStreetMap
import com.example.mad_2024_app.R
import com.example.mad_2024_app.RepositoryProvider
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.DonutRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.DonutViewModel
import com.example.mad_2024_app.view_models.FavoriteShopsViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import com.example.mad_2024_app.view_models.ShopVisitHistoryViewModel
import com.example.mad_2024_app.view_models.UserViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Main : Fragment() {

    private lateinit var shopViewModel: ShopViewModel
    private lateinit var listView: ListView
    private lateinit var favoriteShopsViewModel: FavoriteShopsViewModel
    private lateinit var coordinateViewModel: CoordinateViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var addressViewModel : AddressViewModel
    private lateinit var locationProvider : ILocationProvider
    private lateinit var context: Context

    private lateinit var userViewModel: UserViewModel
    private lateinit var donutsViewModel : DonutViewModel
    private lateinit var shopVisitHistoryViewModel : ShopVisitHistoryViewModel

    private lateinit var userRepo: UserRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository
    private lateinit var favoriteShopsRepo : FavoriteShopsRepository
    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var donutsRepo: DonutRepository
    private lateinit var shopVisitHistoryRepo : ShopVisitHistoryRepository

    private val TAG = "MainFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ListView
        listView = view.findViewById(R.id.lvNearShops)

        initializeViewModels()

        // Setup ShopAdapter and ListView
        val shopAdapter = ShopAdapter(
            requireContext(),
            addressViewModel,
            favoriteShopsViewModel,
            coordinateViewModel,
            requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE),
            requireActivity() as MainActivity
        )

        listView.adapter = shopAdapter

        setupShopObserverForNearbyStores(context, shopAdapter, sharedPreferences)
    }

    private fun initializeViewModels(){
        userRepo = RepositoryProvider.getUserRepository()
        val userFactory = ViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(requireActivity(), userFactory)[UserViewModel::class.java]

        shopRepo = RepositoryProvider.getShopRepository()
        val shopFactory = ViewModelFactory(shopRepo)
        shopViewModel = ViewModelProvider(requireActivity(), shopFactory)[ShopViewModel::class.java]

        addressRepo = RepositoryProvider.getAddressRepository()
        val addressFactory = ViewModelFactory(addressRepo)
        addressViewModel = ViewModelProvider(requireActivity(), addressFactory).get(AddressViewModel::class.java)

        favoriteShopsRepo = RepositoryProvider.getFavoriteShopsRepository()
        val favoriteShopsFactory = ViewModelFactory(favoriteShopsRepo)
        favoriteShopsViewModel = ViewModelProvider(requireActivity(), favoriteShopsFactory).get(FavoriteShopsViewModel::class.java)

        coordinateRepo = RepositoryProvider.getCoordinateRepository()
        val coordinateFactory = ViewModelFactory(coordinateRepo)
        coordinateViewModel = ViewModelProvider(requireActivity(), coordinateFactory).get(CoordinateViewModel::class.java)

        donutsRepo = RepositoryProvider.getDonutRepository()
        val favoriteDonutsFactory = ViewModelFactory(donutsRepo)
        donutsViewModel = ViewModelProvider(requireActivity(), favoriteDonutsFactory).get(DonutViewModel::class.java)

        shopVisitHistoryRepo = RepositoryProvider.getShopVisitHistoryRepository()
        val shopVisitHistoryFactory = ViewModelFactory(shopVisitHistoryRepo)
        shopVisitHistoryViewModel = ViewModelProvider(requireActivity(), shopVisitHistoryFactory).get(
            ShopVisitHistoryViewModel::class.java)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            shopViewModel: ShopViewModel,
            favoriteShopsViewModel: FavoriteShopsViewModel,
            coordinateViewModel: CoordinateViewModel,
            sharedPreferences: SharedPreferences,
            addressViewModel: AddressViewModel,
            locationProvider: ILocationProvider,
            context: Context
        ): Main {
            val fragment = Main()
            fragment.shopViewModel = shopViewModel
            fragment.favoriteShopsViewModel = favoriteShopsViewModel
            fragment.coordinateViewModel = coordinateViewModel
            fragment.sharedPreferences = sharedPreferences
            fragment.addressViewModel = addressViewModel
            fragment.locationProvider = locationProvider
            fragment.context = context
            return fragment
        }
    }

    private fun setupShopObserverForNearbyStores(appContext: Context, shopAdapter : ShopAdapter, sharedPreferences: SharedPreferences) {
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            favoriteShopsViewModel.getFavoriteShopsByUser(userId)
            // Observing favorite shops and updating the adapter's favorite shops set
            favoriteShopsViewModel.favoriteShops.observe(viewLifecycleOwner, Observer { favoriteShopsList ->
                if (favoriteShopsList != null) {
                    // Extracting shop IDs from the favorite shops list
                    val favoriteShopIds = favoriteShopsList.map { it.shopId }.toSet()
                    Log.d(TAG, "Size of favorite shops: ${favoriteShopIds.size}")
                    shopAdapter.setFavoriteShopsIds(favoriteShopIds)
                }
            })

            // Observing shops near coordinates to update the adapter's shop list
            shopViewModel.shopsNearCoordinates.observe(viewLifecycleOwner, Observer { shops ->
                if (shops != null) {
                    shopAdapter.setShops(shops)
                }
            })
        } else {
            // Handle case where user ID is not available
            Log.e(TAG, "User ID not found in SharedPreferences.")
        }
    }

    class ShopAdapter(private val context: Context, private val addressViewModel: AddressViewModel,
                      private val favoriteShopsViewModel: FavoriteShopsViewModel,
                      private val coordinateViewModel: CoordinateViewModel,
                      private val sharedPreferences: SharedPreferences,
                      private val locationProvider: ILocationProvider
    ) : BaseAdapter() {
        private var shops: MutableList<Shop> = mutableListOf()
        private var favoriteShopsIds: Set<Int> = emptySet()
        private val TAG = "ShopAdapter"

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        private lateinit var latestLocation : Location

        fun setShops(newShops: List<Shop>) {
            Log.d(TAG, "Adding Shops")

            // Sort shops so that favorite shops come first
            val sortedShops = newShops.sortedWith(compareBy { !favoriteShopsIds.contains(it.shopId) })

            shops.clear()
            shops.addAll(sortedShops)
            notifyDataSetChanged()

            Log.d(TAG, "Added ${shops.size} shop(s)")
        }

        fun setFavoriteShopsIds(newFavoriteShopsIds: Set<Int>) {
            Log.d(TAG, "Adding Favorite Shops")
            Log.d(TAG, "Added ${newFavoriteShopsIds.size} shop(s)")
            favoriteShopsIds = newFavoriteShopsIds
            notifyDataSetChanged()
        }

        override fun getCount(): Int = shops.size

        override fun getItem(position: Int): Any = shops[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val listItemView = convertView ?: inflater.inflate(R.layout.shop_list_item, parent, false)

            val shop = getItem(position) as Shop
            listItemView.findViewById<TextView>(R.id.shop_name).text = shop.name

            // Fetch and display the address
            shop.addressId?.let { addressId ->
                addressViewModel.getAddressById(addressId) { address ->
                    address?.let {
                        listItemView.findViewById<TextView>(R.id.shop_address).text = formatAddressString(it)
                    }
                }
            }

            // Find the like button (CheckBox) in the inflated view
            val likeButton = listItemView.findViewById<CheckBox>(R.id.like_button)

            // Check if the shop is a favorite and update the checkbox state
            val isFavorite = shop.shopId in favoriteShopsIds
            likeButton.isChecked = isFavorite

            // Set OnCheckedChangeListener for the likeButton (CheckBox)
            likeButton.setOnClickListener { view ->
                val isChecked = likeButton.isChecked
                val uuid = sharedPreferences.getString("userId", null) ?: return@setOnClickListener
                handleFavoriteShopToggle(shop.shopId, isChecked, uuid)
            }

            // Find the map button (ImageView) in the inflated view
            val mapButton = listItemView.findViewById<ImageView>(R.id.map_button)

            shop.locationId?.let { locationId ->
                coordinateViewModel.getCoordinateById(locationId) { coordinate ->
                    coordinate?.let {
                        // Set OnClickListener for the mapButton (ImageView)
                        mapButton.setOnClickListener { view ->
                            latestLocation = locationProvider.getLatestLocation()!!
                            // Inside this block, call the goMaps function and pass the appropriate parameters
                            if (::latestLocation.isInitialized) {
                                val intent = Intent(view.context, OpenStreetMap::class.java).apply {
                                    putExtra("locationBundle", Bundle().apply {
                                        putParcelable("location", latestLocation)
                                    })
                                    putExtra("shopLocation", Bundle().apply {
                                        putDouble("shopLatitude", coordinate.latitude)
                                        putDouble("shopLongitude", coordinate.longitude)
                                    })
                                }
                                view.context.startActivity(intent)
                            } else {
                                Toast.makeText(view.context, "Location not available yet.", Toast.LENGTH_SHORT).show()
                                // Optionally, you can trigger location update here
                            }
                        }
                    }
                }
            }

            return listItemView
        }

        private fun handleFavoriteShopToggle(shopId: Int, isChecked: Boolean, uuid: String) {
            val updatedFavoriteShopsIds = favoriteShopsIds.toMutableSet()

            if (isChecked) {
                updatedFavoriteShopsIds.add(shopId)
                favoriteShopsViewModel.upsertFavoriteShop(FavoriteShops(uuid = uuid, shopId = shopId))
            } else {
                updatedFavoriteShopsIds.remove(shopId)
                favoriteShopsViewModel.removeFavoriteShopById(uuid = uuid, shopId = shopId)
            }

            setFavoriteShopsIds(updatedFavoriteShopsIds)
        }



        private fun formatAddressString(address: Address): String {
            return "${address.street}, ${address.number}\n${address.city}, ${address.zipCode}\n${address.country}"
        }
    }
}