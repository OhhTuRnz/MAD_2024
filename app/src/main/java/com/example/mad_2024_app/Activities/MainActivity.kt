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
import com.example.mad_2024_app.RepositoryProvider
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
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
    private lateinit var donutsViewModel : DonutViewModel
    private lateinit var shopVisitHistoryViewModel : ShopVisitHistoryViewModel

    private lateinit var userRepo: UserRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository
    private lateinit var favoriteShopsRepo : FavoriteShopsRepository
    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var donutsRepo: DonutRepository
    private lateinit var shopVisitHistoryRepo : ShopVisitHistoryRepository

    private lateinit var listView: ListView
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

        applyTheme(sharedPreferences)

        initializeViewModels()

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
            shopVisitHistoryViewModel,
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
    private fun initializeViewModels(){
        userRepo = RepositoryProvider.getUserRepository()
        val userFactory = ViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]

        shopRepo = RepositoryProvider.getShopRepository()
        val shopFactory = ViewModelFactory(shopRepo)
        shopViewModel = ViewModelProvider(this, shopFactory)[ShopViewModel::class.java]

        addressRepo = RepositoryProvider.getAddressRepository()
        val addressFactory = ViewModelFactory(addressRepo)
        addressViewModel = ViewModelProvider(this, addressFactory).get(AddressViewModel::class.java)

        favoriteShopsRepo = RepositoryProvider.getFavoriteShopsRepository()
        val favoriteShopsFactory = ViewModelFactory(favoriteShopsRepo)
        favoriteShopsViewModel = ViewModelProvider(this, favoriteShopsFactory).get(FavoriteShopsViewModel::class.java)

        coordinateRepo = RepositoryProvider.getCoordinateRepository()
        val coordinateFactory = ViewModelFactory(coordinateRepo)
        coordinateViewModel = ViewModelProvider(this, coordinateFactory).get(CoordinateViewModel::class.java)

        donutsRepo = RepositoryProvider.getDonutRepository()
        val favoriteDonutsFactory = ViewModelFactory(donutsRepo)
        donutsViewModel = ViewModelProvider(this, favoriteDonutsFactory).get(DonutViewModel::class.java)

        shopVisitHistoryRepo = RepositoryProvider.getShopVisitHistoryRepository()
        val shopVisitHistoryFactory = ViewModelFactory(shopVisitHistoryRepo)
        shopVisitHistoryViewModel = ViewModelProvider(this, shopVisitHistoryFactory).get(ShopVisitHistoryViewModel::class.java)
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

        saveCoordinatesToFile(location.latitude, location.longitude, filesDir)
        Utils.writeLocationToCSV(this, location)

        updateNearbyStores(location)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        sharedPreferences.edit().apply {
            putString("latestLatitude", location.latitude.toString())
            putString("latestLongitude", location.longitude.toString())
            apply()
        }
    }

    private fun updateNearbyStores(location: Location) {
        // Convert Location to your Coordinate class (if necessary)
        val coordinate = Coordinate(latitude=location.latitude, longitude=location.longitude)

        // Define the radius within which you want to search for stores
        val radius = 5000 // meters

        // Use the ViewModel to fetch stores
        shopViewModel.getAllShopsNearCoordinates(coordinate, radius)

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
        finish()
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