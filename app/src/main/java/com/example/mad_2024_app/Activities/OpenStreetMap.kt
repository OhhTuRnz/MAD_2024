package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import com.example.mad_2024_app.view_models.UserViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

@Suppress("DEPRECATION")
class OpenStreetMap : AppCompatActivity() {
    private val TAG = "LogoGPSOpenStreetMapActivity"

    private lateinit var map: MapView
    private lateinit var latestLocation:Location
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var shopViewModel: ShopViewModel
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var coordinateViewModel: CoordinateViewModel

    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository

    private var shopDetails: MutableList<ShopDetail> = mutableListOf()

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val appContext = application as App

        applyTheme(sharedPreferences)
        initializeViewModels(appContext)
        setContentView(R.layout.activity_open_street_map)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

        map = findViewById(R.id.map)

        setupMapTouchListener()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(18.0)

        Log.d(TAG, "onCreate: The activity OpenMaps is being created.")

        // Assuming locationBundle always exists and contains the "location" extra
        val locationBundle = intent.getBundleExtra("locationBundle")!!
        latestLocation = locationBundle.getParcelable("location")!!

        // Add marker for user's current location
        val userLocation = GeoPoint(latestLocation.latitude, latestLocation.longitude)
        addMarker(userLocation, "My Current Location")
        map.controller.setCenter(userLocation)
        map.controller.setZoom(15.0)  // Default zoom level for user's location

        // Handle shop location if provided
        val shopLatitude = intent.getDoubleExtra("shopLatitude", 0.0)
        val shopLongitude = intent.getDoubleExtra("shopLongitude", 0.0)
        handleShopLocation(shopLatitude, shopLongitude)

        updateNearbyStores(location = latestLocation)
    }

    private fun handleShopLocation(shopLatitude: Double, shopLongitude: Double) {
        if (shopLatitude != 0.0 && shopLongitude != 0.0) {
            val shopLocation = GeoPoint(shopLatitude, shopLongitude)
            addMarker(shopLocation, "Shop Location")
            map.controller.setCenter(shopLocation)
            map.controller.setZoom(25.0)  // Closer zoom for specific shop
        }
    }

    private fun updateShopDetails() {
        shopViewModel.shopsNearCoordinates.observe(this) { shops ->
            shops?.forEach { shop ->
                shop.addressId?.let { addressId ->
                    addressViewModel.getAddressById(addressId) { address ->
                        shop.locationId?.let { locationId ->
                            coordinateViewModel.getCoordinateById(locationId) { coordinate ->
                                // Create a new ShopDetail object and add it to the list
                                val shopDetail = ShopDetail(shop, address, coordinate)
                                Log.d(TAG, "Adding a shop: ${shop.name} ${address?.street} ${coordinate?.latitude} ${coordinate?.longitude}")
                                shopDetails.add(shopDetail)
                                Log.d(TAG, "Shops updated: ${shopDetails.size}")
                                addMarkers(map)
                                addMarkersAndRoute(map)
                                // If needed, update the UI or notify an adapter here
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateNearbyStores(location: Location) {
        val coordinate = Coordinate(latitude=location.latitude, longitude=location.longitude)
        val radius = 5000 // meters

        shopViewModel.getAllShopsNearCoordinates(coordinate, radius)

        updateShopDetails()
    }

    private fun addMarkers(mapView: MapView) {
        for (shopDetail in shopDetails) {
            val marker = Marker(mapView)
            val geoPoint = shopDetail.coordinate?.let { GeoPoint(it.latitude, shopDetail.coordinate.longitude) }
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Marker at ${shopDetail.address?.street} [${geoPoint?.latitude}, ${geoPoint?.longitude}]"
            if (geoPoint != null) {
                addMarker(geoPoint, shopDetail)
            }
            marker.icon = ContextCompat.getDrawable(this, R.drawable.shop_marker)

            marker.relatedObject = shopDetail

            val customInfoWindow = CustomInfoWindow(R.layout.layout_donut_shop_info_window, mapView, this)
            marker.setInfoWindow(customInfoWindow)

            mapView.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set the custom icon for the marker based on the title
        when (title) {
            "My Current Location" -> marker.icon = ContextCompat.getDrawable(this, R.drawable.current_location_marker)
            else -> marker.icon = ContextCompat.getDrawable(this, R.drawable.donut_marker)
        }

        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    private fun addMarker(point: GeoPoint, shopDetail: ShopDetail) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = shopDetail.shop.name

        // Set the custom icon for the marker based on the title
        when (title) {
            "My Current Location" -> marker.icon = ContextCompat.getDrawable(this, R.drawable.current_location_marker)
            else -> marker.icon = ContextCompat.getDrawable(this, R.drawable.donut_marker)
        }

        val customInfoWindow = MarkerInfoWindow(R.layout.layout_donut_shop_info_window, map)
        // Set the custom info window
        marker.setInfoWindow(customInfoWindow)

        // Associate the shop detail with the marker
        marker.relatedObject = shopDetail

        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    /*
    fun addMarkers(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        for (shopDetail in shopDetails) {
            shopDetail.coordinate?.let { coord ->
                val geoPoint = GeoPoint(coord.latitude, coord.longitude)
                addMarker(geoPoint, shopDetail.shop.name)
            }
        }
        map.invalidate() // Refresh the map to display the new markers
    }

     */

    private fun addMarkersAndRoute(mapView: MapView) {
        val routePoints = mutableListOf<GeoPoint>()
        shopDetails.forEach { detail ->
            detail.coordinate?.let { coord ->
                val geoPoint = GeoPoint(coord.latitude, coord.longitude)
                addMarker(geoPoint, detail.shop.name)
                routePoints.add(geoPoint)
            }
        }

        if (routePoints.isNotEmpty()) {
            val route = Polyline().apply {
                setPoints(routePoints)
                color = ContextCompat.getColor(this@OpenStreetMap, R.color.teal_700)
            }
            map.overlays.add(route)
        }
        map.invalidate()
    }

    private fun initializeViewModels(appContext: Context){
        coordinateRepo = DbUtils.getCoordinateRepository(appContext)
        val coordinateFactory = ViewModelFactory(coordinateRepo)
        coordinateViewModel = ViewModelProvider(this, coordinateFactory)[CoordinateViewModel::class.java]

        shopRepo = DbUtils.getShopRepository(appContext)
        val shopFactory = ViewModelFactory(shopRepo)
        shopViewModel = ViewModelProvider(this, shopFactory)[ShopViewModel::class.java]

        addressRepo = DbUtils.getAddressRepository(appContext)
        val addressFactory = ViewModelFactory(addressRepo)
        addressViewModel = ViewModelProvider(this, addressFactory).get(AddressViewModel::class.java)
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
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

    fun onPrevButtonClick(view: View){
        // This is the handler

        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapTouchListener() {
        map.setOnTouchListener { _, event ->
            closeAllInfoWindows()
            false
        }
    }

    private fun closeAllInfoWindows() {
        for (overlay in map.overlays) {
            if (overlay is Marker) {
                overlay.closeInfoWindow()
            }
        }
    }

    data class ShopDetail(
        val shop: Shop,
        val address: Address?,
        val coordinate: Coordinate?
    )

    class CustomInfoWindow(layoutResId: Int, mapView: MapView, private val context: Context) : InfoWindow(layoutResId, mapView) {

        override fun onOpen(item: Any?) {
            val marker = item as Marker
            val shopDetail = marker.relatedObject as ShopDetail  // Ensure you set this when creating the marker

            // Find views
            val nameView = mView.findViewById<TextView>(R.id.tvShopName)
            val descriptionView = mView.findViewById<TextView>(R.id.tvShopDescription)
            val addressView = mView.findViewById<TextView>(R.id.tvShopAddress)
            val commentsView = mView.findViewById<TextView>(R.id.tvShopComments) // TextView or RecyclerView based on your design

            // Set shop details
            nameView.text = shopDetail.shop.name
            descriptionView.text = shopDetail.shop.description
            addressView.text = "${shopDetail.address?.street}, ${shopDetail.coordinate?.latitude}, ${shopDetail.coordinate?.longitude}"

            // Load and display comments for the shop
            // This requires a method to fetch comments from your database or server
            loadComments(shopDetail.shop.shopId, commentsView)
        }

        private fun loadComments(shopId: Int, commentsView: TextView) {
            // Here you would fetch comments for the given shop ID and update the commentsView.
            // For simplicity, it's a TextView, but for a real app, you might use a RecyclerView and an adapter.
        }

        override fun onClose(){
            super.close()
        }
    }
}