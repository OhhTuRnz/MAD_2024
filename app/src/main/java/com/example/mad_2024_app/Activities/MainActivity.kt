package com.example.mad_2024_app.Activities

import Utils
import Utils.Companion.saveCoordinatesToFile
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.Controller.FragmentPageAdapter

import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.FavoriteShops
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.FavoriteDonutsViewModel
import com.example.mad_2024_app.view_models.FavoriteShopsViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import com.example.mad_2024_app.view_models.UserViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), LocationListener, ILocationProvider {

    private lateinit var locationManager: LocationManager
    private lateinit var latestLocation: Location
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var userViewModel: UserViewModel
    private lateinit var shopViewModel: ShopViewModel
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var favoriteShopsViewModel : FavoriteShopsViewModel
    private lateinit var coordinateViewModel : CoordinateViewModel
    private lateinit var favoriteDonutsViewModel : FavoriteDonutsViewModel

    private lateinit var userRepo: UserRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository
    private lateinit var favoriteShopsRepo : FavoriteShopsRepository
    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var favoriteDonutsRepo: FavoriteDonutsRepository

    private lateinit var listView: ListView
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var likeButton: ImageButton

    private val TAG = "LogoGPSMainActivity"

    override fun getLatestLocation(): Location? {
        return if (::latestLocation.isInitialized) latestLocation else null
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val appContext = application as App

        applyTheme(sharedPreferences)

        initializeViewModels(appContext)

        setContentView(R.layout.activity_main)

        setupDrawer()

        setupBottomNav()

        setupFragments()

        storeUserIfNotExisting(sharedPreferences)

        val backgroundImageView: ImageView = findViewById(R.id.donutBackground)
        val gifUrl = "https://art.ngfiles.com/images/2478000/2478561_slavetomyself_spinning-donut-gif.gif?f1650761565"
        Glide.with(this).load(gifUrl).into(backgroundImageView)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupPermissionLauncher()
        checkPermissionsAndStartLocationUpdates()

        //setupShopObserverForNearbyStores(appContext, sharedPreferences)

        Log.d(TAG, "onCreate: Main activity is being created")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "OnDestroy: MAIN DESTROYED")
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        // Reset isFirstOpen to true when the app is closed or sent to the background
        with(sharedPreferences.edit()) {
            putBoolean("isFirstOpen", true)
            apply()
        }
    }

    private fun setupFragments(){
        //TabLayout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        adapter = FragmentPageAdapter(
            supportFragmentManager,
            lifecycle,
            shopViewModel,
            favoriteShopsViewModel,
            coordinateViewModel,
            sharedPreferences,
            addressViewModel,
            this,
            this // for the context
        )

        tabLayout.addTab(tabLayout.newTab().setText("Cercanas"))
        tabLayout.addTab(tabLayout.newTab().setText("Recientes"))

        viewPager2.adapter = adapter

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
        //Fin
    }

    private fun setupShopObserverForNearbyStores(appContext: Context, sharedPreferences: SharedPreferences) {
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            // Initialize an empty set to hold favorite shop IDs
            val favoriteShops = mutableSetOf<Int>()

            // Initialize the ListView and adapter

            val listView = findViewById<ListView>(R.id.lvNearShops)
            val shopAdapter = ShopAdapter(this, addressViewModel, favoriteShopsViewModel, coordinateViewModel, sharedPreferences, this)
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

            // Observing shops near coordinates to update the adapter's shop list
            shopViewModel.shopsNearCoordinates.observe(this, Observer { shops ->
                if (shops != null) {
                    shopAdapter.setShops(shops)
                }
            })
        } else {
            // Handle case where user ID is not available
            Log.e(TAG, "User ID not found in SharedPreferences.")
        }
    }

    private fun initializeViewModels(appContext: Context){
        userRepo = DbUtils.getUserRepository(appContext)
        val userFactory = ViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]

        shopRepo = DbUtils.getShopRepository(appContext)
        val shopFactory = ViewModelFactory(shopRepo)
        shopViewModel = ViewModelProvider(this, shopFactory)[ShopViewModel::class.java]

        addressRepo = DbUtils.getAddressRepository(appContext)
        val addressFactory = ViewModelFactory(addressRepo)
        addressViewModel = ViewModelProvider(this, addressFactory).get(AddressViewModel::class.java)

        favoriteShopsRepo = DbUtils.getFavoriteShopsRepository(appContext)
        val favoriteShopsFactory = ViewModelFactory(favoriteShopsRepo)
        favoriteShopsViewModel = ViewModelProvider(this, favoriteShopsFactory).get(FavoriteShopsViewModel::class.java)

        coordinateRepo = DbUtils.getCoordinateRepository(appContext)
        val coordinateFactory = ViewModelFactory(coordinateRepo)
        coordinateViewModel = ViewModelProvider(this, coordinateFactory).get(CoordinateViewModel::class.java)

        favoriteDonutsRepo = DbUtils.getFavoriteDonutsRepository(appContext)
        val favoriteDonutsFactory = ViewModelFactory(favoriteDonutsRepo)
        favoriteDonutsViewModel = ViewModelProvider(this, favoriteDonutsFactory).get(FavoriteDonutsViewModel::class.java)
    }

    private fun storeUserIfNotExisting(sharedPreferences: SharedPreferences) {
        var userUUID = sharedPreferences.getString("userId", null)

        if (userUUID == null) {
            // Generate a new UUID
            userUUID = UUID.randomUUID().toString()
            Log.d(TAG, "Generated new UUID: $userUUID")

            // Store the new UUID in SharedPreferences
            sharedPreferences.edit().apply {
                putString("userId", userUUID)
                apply()
            }
        }
        if(FirebaseAuth.getInstance().currentUser == null) {
            sharedPreferences.edit().apply {
                putString("anonymousUserId", userUUID)
                apply()
            }
        }

        // Check and store user (the method will handle insertion if user doesn't exist)
        userViewModel.checkAndStoreUser(userUUID)
    }

    private fun applyTheme(sharedPreferences: SharedPreferences){
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    fun onNextButtonClick(view: View) {
        if (::latestLocation.isInitialized) {
            val intent = Intent(this, SecondActivity::class.java).apply {
                putExtra("locationBundle", Bundle().apply {
                    putParcelable("location", latestLocation)
                })
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndStartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favorites -> {
                    // Handle favorites action
                    Toast.makeText(applicationContext, "Favorites", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goFavorite(rootView)
                    true
                }

                R.id.nav_maps -> {
                    // Handle maps action
                    Toast.makeText(applicationContext, "OpenStreetMaps", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goMaps(rootView)
                    true
                }

                R.id.nav_donuts -> {
                    // Handle nearby donuts action
                    Toast.makeText(applicationContext, "FavDonuts", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goFavDonuts(rootView)
                    true
                }

                else -> false
            }
        }
    }
    private fun setupDrawer(){
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
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        // Get last known location immediately
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        }
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Check for location permissions
            val granted = permissions.entries.all { it.value }
            if (granted) {
                // PERMISSION GRANTED
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f,
                        this
                    )
                }
            } else {
                // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
                // whichever happens first
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        latestLocation = location
        runOnUiThread {
            val textView: TextView = findViewById(R.id.mainTextView)
            textView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        }
        saveCoordinatesToFile(location.latitude, location.longitude, filesDir)
        Utils.writeLocationToCSV(this, location)

        updateNearbyStores(location)
    }

    private fun updateNearbyStores(location: Location) {
        // Convert Location to your Coordinate class (if necessary)
        val coordinate = Coordinate(latitude=location.latitude, longitude=location.longitude)

        // Define the radius within which you want to search for stores
        val radius = 5000 // meters

        // Use the ViewModel to fetch stores
        shopViewModel.getAllShopsNearCoordinates(coordinate, radius)

    }

    class ShopAdapter(private val context: Context, private val addressViewModel: AddressViewModel,
                      private val favoriteShopsViewModel: FavoriteShopsViewModel,
                      private val coordinateViewModel: CoordinateViewModel,
                      private val sharedPreferences: SharedPreferences,
                      private val locationProvider: ILocationProvider) : BaseAdapter() {
        private var shops: MutableList<Shop> = mutableListOf()
        private var favoriteShopsIds: Set<Int> = emptySet()
        private val TAG = "ShopAdapter"

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        private lateinit var latestLocation : Location

        private val executor: ExecutorService = Executors.newSingleThreadExecutor()

        fun setShops(newShops: List<Shop>) {
            Log.d(TAG, "Adding Shops")
            Log.d(TAG, "Added ${shops.size} shop(s)")
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
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
            val likeButton = listItemView.findViewById<CheckBox>(R.id.like_button) as CheckBox

            // Check if the shop is a favorite and update the checkbox state
            val isFavorite = shop.shopId in favoriteShopsIds
            likeButton.isChecked = isFavorite

            // Set OnCheckedChangeListener for the likeButton (CheckBox)
            likeButton.setOnCheckedChangeListener { buttonView, isChecked ->
                // Inside this block, you can put your logic for handling the checkbox state change
                Log.d(TAG, "Checkbox state changed: $isChecked")

                val uuid = sharedPreferences.getString("userId", null)

                if (uuid == null) {
                    Log.e(TAG, "User ID is null.")
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {
                    Log.d(TAG, "Adding shop ${shop.shopId} to favorites.")
                    executor.execute {
                        favoriteShopsViewModel.upsertFavoriteShop(
                            FavoriteShops(
                                uuid = uuid,
                                shopId = shop.shopId
                            )
                        )
                    }
                } else {
                    Log.d(TAG, "Removing shop ${shop.shopId} from favorites.")
                    executor.execute {
                        favoriteShopsViewModel.removeFavoriteShopById(
                            uuid = uuid,
                            shopId = shop.shopId
                        )
                    }
                }
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

        private fun isShopFavorite(shopId: Int): Boolean {
            val favoriteShopIds: Set<Int>? = favoriteShopsIds
            return favoriteShopIds?.contains(shopId) ?: false
        }



        private fun formatAddressString(address: Address): String {
            return "${address.street}, ${address.number}\n${address.city}, ${address.zipCode}\n${address.country}"
        }
    }

    @Deprecated("This declaration overrides deprecated member but not marked as deprecated itself. Please add @Deprecated annotation or suppress. See https://youtrack.jetbrains.com/issue/KT-47902 for details")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goHome(view: View){
        // go to Main
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goMaps(view: View){
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

    fun goSettings(view: View){
        // go to Settings
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goLogin(view: View){
        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, show a toast message
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show()
        } else {
            // User is not logged in, go to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goProfile(view: View){
        // go to Settings
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun goFavorite(view: View) {
        val intent = Intent(this, FavoriteShopsActivity::class.java)
        startActivity(intent)
    }

    private fun goFavDonuts(view: View) {
        val intent = Intent(this, FavoriteDonutsActivity::class.java)
        startActivity(intent)
    }


    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        if (auth.currentUser != null) {
            // User is logged in, proceed with logout
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val userUUID = sharedPreferences.getString("anonymousUserId", null)

            sharedPreferences.edit().apply {
                putString("userId", userUUID)
                apply()
            }
            // Redirect to login screen or another appropriate activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optionally, close the current activity
        } else {
            // User is not logged in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}