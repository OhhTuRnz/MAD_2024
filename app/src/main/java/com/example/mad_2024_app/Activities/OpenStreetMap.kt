package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mad_2024_app.App
import com.example.mad_2024_app.Controller.ShopDetailsFragment
import com.example.mad_2024_app.R
import com.example.mad_2024_app.RepositoryProvider
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.ShopVisitHistory
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.ShopVisitHistoryRepository
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import com.example.mad_2024_app.view_models.ShopVisitHistoryViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.util.concurrent.Executors
import kotlinx.coroutines.*

private val permanentMarkers: MutableList<Marker> = mutableListOf()

class OpenStreetMap : AppCompatActivity() {
    private val TAG = "LogoGPSOpenStreetMapActivity"

    private lateinit var map: MapView
    private lateinit var latestLocation:Location
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var shopViewModel: ShopViewModel
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var coordinateViewModel: CoordinateViewModel
    private lateinit var shopVisitHistoryViewModel: ShopVisitHistoryViewModel

    private lateinit var coordinateRepo: CoordinateRepository
    private lateinit var shopRepo: ShopRepository
    private lateinit var addressRepo: AddressRepository
    private lateinit var shopVisitHistoryRepo: ShopVisitHistoryRepository

    private var shopDetails: MutableList<ShopDetail> = mutableListOf()
    private var clusters: MutableList<MarkerCluster> = mutableListOf()
    private var showingIndividualMarkers = false

    private var lastShopProcessedTime = 0L
    private val updateDelay = 1000L

    private var nearShopRadius = 5000
    private var clusterRadius = 1000

    private var debounceJob: Job? = null

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Assuming locationBundle always exists and contains the "location" extra
        val locationBundle = intent.getBundleExtra("locationBundle")!!
        latestLocation = locationBundle.getParcelable("location")!!

        applyTheme(sharedPreferences)
        initializeViewModels()
        setContentView(R.layout.activity_open_street_map)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

        map = findViewById(R.id.map)

        setupMapTouchListener()

        map.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                return false
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                val currentCenter = map.mapCenter
                val currentLocation = Location("").apply {
                    latitude = currentCenter.latitude
                    longitude = currentCenter.longitude
                }

                displayIndividualMarkersIfNeeded(currentLocation)
                displayClustersIfNeeded()

                Log.d(TAG, "Zoom level: ${map.zoomLevelDouble} and showingIndividualMarkers: $showingIndividualMarkers")

                return false
            }
        })

        map.setTileSource(TileSourceFactory.MAPNIK)

        Log.d(TAG, "onCreate: The activity OpenMaps is being created.")

        val userLocation = addCurrentLocationMarker()

        val shopLocationBundle = intent.getBundleExtra("shopLocation")
        val shopLatitude = shopLocationBundle?.getDouble("shopLatitude", Double.POSITIVE_INFINITY) ?: Double.POSITIVE_INFINITY
        val shopLongitude = shopLocationBundle?.getDouble("shopLongitude", Double.POSITIVE_INFINITY) ?: Double.POSITIVE_INFINITY


        if(shopLatitude == Double.POSITIVE_INFINITY || shopLongitude == Double.POSITIVE_INFINITY){
            map.controller.setCenter(userLocation)
            map.controller.setZoom(15.0)
            updateNearbyStores(location = latestLocation, nearShopRadius)
        }
        else{
            val shopLocation = Location("").apply {
                latitude = shopLatitude
                longitude = shopLongitude
            }
            handleShopLocation(shopLatitude, shopLongitude)
            updateNearbyStores(location = shopLocation, 50)
        }
    }

    private fun addCurrentLocationMarker() : GeoPoint{
        val userLocation = GeoPoint(latestLocation.latitude, latestLocation.longitude)
        addMarker(userLocation, "My Current Location")

        return userLocation
    }

    private fun displayIndividualMarkersIfNeeded(location: Location) {
        if (map.zoomLevelDouble > 15 && showingIndividualMarkers) {
            displayIndividualMarkers(location)
        }
    }

    private fun displayClustersIfNeeded() {
        if (map.zoomLevelDouble <= 15) {
            displayClusters()
        }
    }

    private fun displayIndividualMarkers(location: Location) {
        updateNearbyStores(location = location, clusterRadius)
        showingIndividualMarkers = true
    }

    private fun displayClusters() {
        // Redo clustering and add cluster markers
        Log.d(TAG, "Clusters being displayed")
        updateNearbyStores(location = latestLocation, nearShopRadius)
        showingIndividualMarkers = false
    }

    private fun handleShopLocation(shopLatitude: Double, shopLongitude: Double) {
        if (shopLatitude != Double.POSITIVE_INFINITY && shopLongitude != Double.POSITIVE_INFINITY) {
            val shopLocation = GeoPoint(shopLatitude, shopLongitude)

            Log.d(TAG, "Adding shop from list to map")

            map.controller.setCenter(shopLocation)
            map.controller.setZoom(17.0)
        }
    }

    private fun updateShopDetails() {
        shopViewModel.shopsNearCoordinates.observe(this) { shops ->
            debounceJob?.cancel() // Cancel the previous debounce job if any
            debounceJob = CoroutineScope(Dispatchers.Main).launch {
                delay(updateDelay)
                if (shops != null) {
                    updateShops(shops)
                }
            }
        }
    }

    private suspend fun updateShops(shops: List<Shop>) {
        shopDetails.clear()

        shops.forEach { shop ->
            shop.addressId?.let { addressId ->
                addressViewModel.getAddressById(addressId) { address ->
                    shop.locationId?.let { locationId ->
                        coordinateViewModel.getCoordinateById(locationId) { coordinate ->
                            val shopDetail = ShopDetail(shop, address, coordinate)
                            shopDetails.add(shopDetail)
                            lastShopProcessedTime = System.currentTimeMillis()
                            delayAndUpdateMarkers()

                            // Stop observing if individual markers are displayed
                            if (showingIndividualMarkers) {
                                shopViewModel.shopsNearCoordinates.removeObservers(this@OpenStreetMap)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun delayAndUpdateMarkers() {
        map.postDelayed({
            if (System.currentTimeMillis() - lastShopProcessedTime >= updateDelay) {
                addMarkers(map)

                // Update clusters after adding markers
                if (!showingIndividualMarkers) {
                    updateClustersIfNeeded()
                    addClusterMarkersToMap(map)
                }
            }
        }, updateDelay)
    }

    private fun updateClustersIfNeeded() {
        // Compare existing clusters with new shop details
        // Update clusters only if there are changes
        val newShopsSet = shopDetails.map { it.shop.shopId }.toSet()
        val existingShopsSet = clusters.flatMap { it.markers.map { marker -> (marker.relatedObject as ShopDetail).shop.shopId } }.toSet()

        if (newShopsSet != existingShopsSet) {
            // Recreate clusters
            clusters.clear()
            createClusters()
            addClusterMarkersToMap(map)
        }
    }

    private fun createClusters() {
        //val clusterRadius = 1000.0 // Radius in meters for clustering

        for (shopDetail in shopDetails) {
            var addedToCluster = false
            val shopGeoPoint = shopDetail.coordinate?.let { GeoPoint(it.latitude, it.longitude) }

            for (cluster in clusters) {
                val distance = shopGeoPoint?.distanceToAsDouble(cluster.geoPoint)
                if (distance != null && distance < clusterRadius) {
                    cluster.markers.add(addShopMarker(map, shopDetail, shopGeoPoint))
                    addedToCluster = true
                    break
                }
            }

            if (!addedToCluster && shopGeoPoint != null) {
                val newCluster = MarkerCluster(mutableListOf(addShopMarker(map, shopDetail, shopGeoPoint)), shopGeoPoint)
                clusters.add(newCluster)
            }
        }
    }

    private fun addClusterMarkersToMap(mapView: MapView) {
        for (cluster in clusters) {
            if (cluster.markers.size <= 3) {
                addMarker(cluster.geoPoint!!, cluster.markers.first().title)
                mapView.overlays.add(cluster.markers.first())
            } else {
                val clusterMarker = Marker(mapView)
                clusterMarker.position = cluster.geoPoint
                clusterMarker.icon = createClusterIcon(cluster.markers.size)
                clusterMarker.title = "Cluster with ${cluster.markers.size} shops"
                clusterMarker.setOnMarkerClickListener { _, _ ->
                    handleClusterClick(clusterMarker, mapView)
                    true
                }
                mapView.overlays.add(clusterMarker)
            }
        }
    }

    private fun updateNearbyStores(location: Location, radius: Int) {
        val coordinate = Coordinate(latitude=location.latitude, longitude=location.longitude)

        shopViewModel.getAllShopsNearCoordinates(coordinate, radius)

        updateShopDetails()
    }

    private fun addMarkers(mapView: MapView) {
        map.overlays.removeAll { it !in permanentMarkers } // Remove all markers except the current location marker

        if (showingIndividualMarkers) {
            // Add individual shop markers
            Log.d(TAG, "Displaying individual markers")
            for (shopDetail in shopDetails) {
                val shopGeoPoint = shopDetail.coordinate?.let { GeoPoint(it.latitude, it.longitude) }
                shopGeoPoint?.let {
                    mapView.overlays.add(addShopMarker(mapView, shopDetail, it))
                    addMarker(shopGeoPoint, shopDetail)
                }
            }
        } else {
            // Add cluster markers
            addClusterMarkersToMap(mapView)
        }

        mapView.invalidate()
    }

    private fun handleClusterClick(clusterMarker: Marker, mapView: MapView) {
        // Retrieve the cluster's location
        val clusterLocation = clusterMarker.position

        // Change the zoom level if desired
        val zoomLevel = mapView.zoomLevelDouble + 1  // Zoom in closer
        mapView.controller.animateTo(clusterLocation, zoomLevel, 1000L)

        // Indicate that individual markers should be displayed
        showingIndividualMarkers = true
    }

    private fun addShopMarker(mapView: MapView, shopDetail: ShopDetail, geoPoint: GeoPoint): Marker {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = shopDetail.shop.name
        marker.icon = ContextCompat.getDrawable(this, R.drawable.shop_marker) // Replace with your shop icon
        marker.relatedObject = shopDetail

        val customInfoWindow = CustomInfoWindow(R.layout.layout_donut_shop_info_window, mapView, this, shopVisitHistoryViewModel = shopVisitHistoryViewModel)
        marker.setInfoWindow(customInfoWindow)

        return marker
    }

    // Cluster data class
    data class MarkerCluster(
        val markers: MutableList<Marker> = mutableListOf(),
        val geoPoint: GeoPoint? = null
    )

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set the custom icon for the marker based on the title
        when (title) {
            "My Current Location" -> {
                marker.icon = ContextCompat.getDrawable(this, R.drawable.current_location_marker)
            }
            else -> marker.icon = ContextCompat.getDrawable(this, R.drawable.shop_marker)
        }

        permanentMarkers.add(marker)

        map.overlays.add(marker)
        map.invalidate()
    }

    private fun addMarker(point: GeoPoint, shopDetail: ShopDetail) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = shopDetail.shop.name

        marker.icon = ContextCompat.getDrawable(this, R.drawable.donut_marker)

        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    private fun createClusterIcon(clusterSize: Int): Drawable {
        val baseIcon = ContextCompat.getDrawable(this, R.drawable.cluster_marker) // Your base icon
        val bitmap = Bitmap.createBitmap(baseIcon!!.intrinsicWidth, baseIcon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        baseIcon.setBounds(0, 0, canvas.width, canvas.height)
        baseIcon.draw(canvas)

        // Drawing the text (number of items in the cluster) on the icon
        val text = clusterSize.toString()
        val paint = Paint().apply {
            color = Color.BLACK // Text color
            textSize = 50F // Set text size
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val textX = canvas.width / 2f // X position for text
        val textY = (canvas.height / 2f - (paint.descent() + paint.ascent()) / 2f) // Y position for text

        canvas.drawText(text, textX, textY, paint)

        return BitmapDrawable(resources, bitmap)
    }

    private fun initializeViewModels(){
        coordinateRepo = RepositoryProvider.getCoordinateRepository()
        val coordinateFactory = ViewModelFactory(coordinateRepo)
        coordinateViewModel = ViewModelProvider(this, coordinateFactory)[CoordinateViewModel::class.java]

        shopRepo = RepositoryProvider.getShopRepository()
        val shopFactory = ViewModelFactory(shopRepo)
        shopViewModel = ViewModelProvider(this, shopFactory)[ShopViewModel::class.java]

        addressRepo = RepositoryProvider.getAddressRepository()
        val addressFactory = ViewModelFactory(addressRepo)
        addressViewModel = ViewModelProvider(this, addressFactory).get(AddressViewModel::class.java)

        shopVisitHistoryRepo = RepositoryProvider.getShopVisitHistoryRepository()
        val shopVisitHistoryFactory = ViewModelFactory(shopVisitHistoryRepo)
        shopVisitHistoryViewModel = ViewModelProvider(this, shopVisitHistoryFactory).get(ShopVisitHistoryViewModel::class.java)
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

    class CustomInfoWindow(layoutResId: Int, mapView: MapView, private val context: Context, private val shopVisitHistoryViewModel: ShopVisitHistoryViewModel) : InfoWindow(layoutResId, mapView) {

        private val TAG = "MarkerCustomInfoWindow"

        override fun onOpen(item: Any?) {
            val marker = item as Marker
            val shopDetail = marker.relatedObject as ShopDetail

            // Find views
            val nameView = mView.findViewById<TextView>(R.id.tvShopName)
            val addressView = mView.findViewById<TextView>(R.id.tvShopAddress)
            val overallRatingBarView = mView.findViewById<RatingBar>(R.id.overallRatingBar)

            val commentsContainer = mView.findViewById<LinearLayout>(R.id.llCommentsContainer)

            val progressBar = mView.findViewById<ProgressBar>(R.id.progressBar)

            val goButton = mView.findViewById<Button>(R.id.go_button)

            val moreButton = mView.findViewById<Button>(R.id.more_button)

            goButton.setOnClickListener {
                shopDetail.coordinate?.let { it1 -> openGoogleMaps(context, shopDetail.coordinate.latitude, it1.longitude, shopDetail.shop.name, shopDetail.shop.shopId, shopVisitHistoryViewModel) }
                this.close()
            }

            moreButton.setOnClickListener {
                openDetailedShopView(shopDetail)
                this.close()
            }

            // Show progress bar initially
            commentsContainer.visibility = View.GONE
            overallRatingBarView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            // Set shop details
            nameView.text = shopDetail.shop.name
            addressView.text =
                "${shopDetail.address?.street}, ${shopDetail.address?.number}, ${shopDetail.address?.zipCode}"

            // Load and display comments for the shop
            // This requires a method to fetch comments from your database or server
            loadComments(shopDetail, commentsContainer, overallRatingBarView, progressBar, context)
        }

        private fun loadComments(shopDetail: ShopDetail, commentsContainer: LinearLayout, overallRatingBar : RatingBar, progressBar: ProgressBar, context: Context) {
            val db = Firebase.firestore
            val shopName = shopDetail.shop.name.replace(" ", "_")
            Log.d(TAG, "Loading comment. shopId: ${shopName+"@"+shopDetail.coordinate?.longitude+";"+shopDetail.coordinate?.latitude}")
            db.collection("comments")
                .whereEqualTo("shopId", shopName+"@"+shopDetail.coordinate?.longitude.toString()+";"+shopDetail.coordinate?.latitude.toString())
                .orderBy("timestamp", Query.Direction.DESCENDING) // If you have a timestamp field
                .get()
                .addOnSuccessListener { documents ->
                    commentsContainer.removeAllViews()
                    Log.d(TAG, "Success on retrieving comments")
                    var totalRating = 0.0
                    var ratingCount = 0
                    val ratings = mutableListOf<Double>()

                    for (document in documents) {
                        val commentText = document.getString("comment") ?: "No comments yet"
                        val ratingValue = document.getDouble("rating") ?: 0.0
                        totalRating += ratingValue
                        ratingCount++

                        val commentView = TextView(context).apply {
                            text = commentText
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val ratingBar = RatingBar(context, null, android.R.attr.ratingBarStyleSmall).apply {
                            rating = ratingValue.toFloat()
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            )
                        }

                        ratings.add(ratingValue)

                        // Add the comment and rating to the container
                        commentsContainer.addView(commentView)
                        commentsContainer.addView(ratingBar)
                    }
                    if (ratings.isNotEmpty()) {
                        val medianRating = calculateMedian(ratings)
                        overallRatingBar.rating = medianRating.toFloat()
                    } else {
                        overallRatingBar.rating = 0f
                    }

                    progressBar.visibility = View.GONE
                    commentsContainer.visibility = View.VISIBLE
                    overallRatingBar.visibility = View.VISIBLE

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting comments: ", exception)
                    commentsContainer.removeAllViews()

                    val errorTextView = TextView(context).apply {
                        text = "Failed to load comments."
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    commentsContainer.addView(errorTextView) // Add the error message to the container

                    progressBar.visibility = View.GONE
                    commentsContainer.visibility = View.VISIBLE
                    overallRatingBar.visibility = View.VISIBLE
                }
        }

        private fun calculateMedian(ratings: MutableList<Double>): Double {
            if (ratings.isEmpty()) return 0.0

            val sortedRatings = ratings.sorted()
            val middle = sortedRatings.size / 2

            return if (sortedRatings.size % 2 == 0) {
                (sortedRatings[middle - 1] + sortedRatings[middle]) / 2.0
            } else {
                sortedRatings[middle]
            }
        }

        private fun openDetailedShopView(shopDetail: ShopDetail) {
            val shopDetailsFragment = ShopDetailsFragment.newInstance(
                shopDetail.shop.name,
                "${shopDetail.address?.street}, ${shopDetail.address?.number}, ${shopDetail.address?.zipCode}",
                shopDetail.coordinate?.latitude ?: Double.POSITIVE_INFINITY,
                shopDetail.coordinate?.longitude ?: Double.POSITIVE_INFINITY
            )
            shopDetailsFragment.show((context as FragmentActivity).supportFragmentManager, "shopDetails")
        }

        private fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String, shopId : Int, shopVisitHistoryViewModel: ShopVisitHistoryViewModel) {
            val encodedLabel = Uri.encode(label)
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val uuid = sharedPreferences.getString("userId", null)

            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                uuid?.let { ShopVisitHistory(visitorUuid = it, visitedShopId = shopId, timestamp = System.currentTimeMillis() / 1000) }
                    ?.let { shopVisitHistoryViewModel.upsert(it) }
            }

            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                Toast.makeText(context, "No Google Maps found", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onClose(){
            super.close()
        }
    }
}