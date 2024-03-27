package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.FavoriteShopsViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class FavoriteShopsActivity : AppCompatActivity() {

    private val TAG = "LogoGPSFavShopsActivity"
    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var favoriteShopsViewModel: FavoriteShopsViewModel
    private lateinit var addressViewModel: AddressViewModel

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val appContext = application as App
        addressViewModel = ViewModelProvider(this).get(AddressViewModel::class.java)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_favorite_shops)

        toggleDrawer()

        setupShopObserverForNearbyStores(appContext,sharedPreferences)
    }

    private fun setupShopObserverForNearbyStores(appContext: Context, sharedPreferences: SharedPreferences) {
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            // Initialize an empty set to hold favorite shop IDs
            val favoriteShops = mutableSetOf<Int>()

            // Initialize the ListView and adapter
            val listView = findViewById<ListView>(R.id.lvShops)
            val shopAdapter = FavoriteShopAdapter(
                this,
                addressViewModel,
            )
            listView.adapter = shopAdapter

            favoriteShopsViewModel.getFavoriteShopsByUser(userId)
            // Observing favorite shops and updating the adapter's favorite shops set
            favoriteShopsViewModel.favoriteShops.observe(this, Observer { favoriteShopsList ->
                if (favoriteShopsList != null) {
                    // Extracting shop IDs from the favorite shops list
                    val favoriteShopIds = favoriteShopsList.map { it.shopId }.toSet()
                    Log.d(TAG, "Size of favorite shops: ${favoriteShopIds.size}")
                    shopAdapter.setFavoriteShopsIds(favoriteShopIds)
                }
            })
        } else {
            // Handle case where user ID is not available
            Log.e(TAG, "User ID not found in SharedPreferences.")
        }
    }


    private fun applyTheme(sharedPreferences: SharedPreferences) {
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun toggleDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_drawer)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // Handle nav_home click (Home)
                    val rootView = findViewById<View>(android.R.id.content)
                    goHome(rootView)
                    true
                }

                R.id.nav_maps -> {
                    // Handle nav_maps click (OpenStreetMaps)
                    Toast.makeText(applicationContext, "OpenStreetMaps", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goMaps(rootView)
                    true
                }

                R.id.nav_settings -> {
                    // Handle nav_settings click (Settings)
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goSettings(rootView)
                    true
                }

                R.id.nav_login -> {
                    // Handle nav_login click (Login)
                    val rootView = findViewById<View>(android.R.id.content)
                    goLogin(rootView)
                    true
                }

                R.id.nav_profile -> {
                    // Handle nav_profile click (Profile)
                    Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goProfile(rootView)
                    true
                }

                R.id.nav_logout -> {
                    logoutUser()
                    true
                }

                else -> false
            }
        }
    }

    fun onPrevButtonClick(view: View) {
        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goHome(view: View) {
        // go to Main
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    fun goMaps(view: View) {
        // go to OpenStreetMaps
        if (::latestLocation.isInitialized) {
            val intent = Intent(this, OpenStreetMap::class.java).apply {
                putExtra("locationBundle", Bundle().apply {
                    putParcelable("location", latestLocation)
                })
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    fun goSettings(view: View) {
        // go to Settings
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun goProfile(view: View) {
        // go to Settings
        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
    }

    private fun goLogin(view: View) {
        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, show a toast message and do not navigate
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show()
        } else {
            // User is not logged in, navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Optionally, you can remove this toast to avoid redundancy
            Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is logged in, proceed with logout
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Redirect to login screen or another appropriate activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optionally, close the current activity
        } else {
            // User is not logged in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }


    class FavoriteShopAdapter(
        private val context: Context,
        private val addressViewModel: AddressViewModel
    ) : BaseAdapter() {
        private var shops: MutableList<Shop> = mutableListOf()
        private var favoriteShopsIds: Set<Int> = emptySet()

        private val TAG = "ShopAdapter"
        fun setShops(newShops: List<Shop>) {
            Log.d(TAG, "Adding Shops")
            Log.d(TAG, "Added ${shops.size} shop(s)")
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
        }

        override fun getCount(): Int = shops.size

        override fun getItem(position: Int): Any = shops[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.shop_list_item, parent, false)

            val shop = getItem(position) as Shop
            listItemView.findViewById<TextView>(R.id.shop_name).text = shop.name

            // Fetch and display the address
            shop.addressId?.let { addressId ->
                addressViewModel.getAddressById(addressId) { address ->
                    address?.let {
                        // Update the UI with the address
                        listItemView.findViewById<TextView>(R.id.shop_address).text =
                            formatAddressString(it)
                    }
                }
            }

            return listItemView
        }

        fun setFavoriteShopsIds(newFavoriteShopsIds: Set<Int>) {
            Log.d(TAG, "Adding Favorite Shops")
            Log.d(TAG, "Added ${newFavoriteShopsIds.size} shop(s)")
            favoriteShopsIds = newFavoriteShopsIds
            notifyDataSetChanged()
        }

        private fun formatAddressString(address: Address): String {
            return "${address.street}, ${address.number}\n${address.city}, ${address.zipCode}\n${address.country}"
        }
    }
}