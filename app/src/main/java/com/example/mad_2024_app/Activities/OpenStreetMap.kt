package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.view_models.AddressViewModel
import com.example.mad_2024_app.view_models.CoordinateViewModel
import com.example.mad_2024_app.view_models.ShopViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class OpenStreetMap : AppCompatActivity() {
    private val TAG = "LogoGPSOpenStreetMapActivity"
    private lateinit var map: MapView
    private lateinit var latestLocation:Location
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var shopViewModel: ShopViewModel
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var coordinateViewModel: CoordinateViewModel

    private var shops: List<Shop> = emptyList()
    private var addresses: List<Address> = emptyList()
    private var coordinates: List<Coordinate> = emptyList()

    val gymkhanaCoords = listOf(
        //GeoPoint(40.38779608214728, -3.627687914352839), // Tennis
        //GeoPoint(40.38788595319803, -3.627048250272035), // Futsal outdoors
        //GeoPoint(40.3887315224542, -3.628643539758645), // Fashion and design
        //GeoPoint(40.38926842612264, -3.630067893975619), // Topos
        //GeoPoint(40.38956358584258, -3.629046081389352), // Teleco
        //GeoPoint(40.38992125672989, -3.6281366497769714), // ETSISI
        //GeoPoint(40.39037466191718, -3.6270256763598447), // Library
        //GeoPoint(40.389855884803005, -3.626782180787362), // CITSEM
        GeoPoint(40.5103921,-3.69627951671),
        GeoPoint(40.30216386637232, -3.4402212966369747), // Granier Arganda
        GeoPoint(40.33789633833347, -3.5300499470893385), // DUNKIN Rivas H20
        GeoPoint(40.383173612528864, -3.6268099771703244), // LIDL UPM
        GeoPoint(40.382939957549375, -3.628704563477796), // Supeco UPM
        GeoPoint(40.38296744641256, -3.6249153908628533), // Mercadona UPM
        GeoPoint(40.38627977232809, -3.62547474503236), // Chef Rosa UPM
        GeoPoint(40.386953212854976, -3.6361746467497937), // Pan Familiar UPM
        GeoPoint(40.39267993157272, -3.624293651826863), // Carrefour Express UPM
        GeoPoint(40.37271470034199, -3.5915906061005254), // DUNKIN La Gavia
        GeoPoint(40.39165928008534, -3.7003710177221287), // DUNKIN Plaza Rio 2
        GeoPoint(40.40915611015745, -3.692045723585773), // DUNKIN Atocha
        GeoPoint(40.42505522977919, -3.70327409511652), // LULULU Artesanal Donuts Tribunal
        GeoPoint(40.51086830465546, -3.695438356003822), // Panaix Bakery & Coffee Montecarmelo
        GeoPoint(40.4172309597888, -3.6738480248533203) // Rousquillas Bakery Ibiza

    )
    val gymkhanaNames = listOf(
        //"Tennis",
        //"Futsal outdoors",
        //"Fashion and design school",
        //"Topography school",
        //"Telecommunications school",
        //"ETSISI",
        //"Library",
        //"CITSEM",
        "Donut_shop_test",
        "Granier Arganda del Rey",
        "DUNKIN Rivas H20",
        "LIDL UPM",
        "Supeco UPM",
        "Mercadona UPM",
        "Chef Rosa UPM",
        "Pan Familiar UPM",
        "Carrefour Express UPM",
        "DUNKIN La Gavia",
        "DUNKIN Plaza Rio 2",
        "DUNKIN Atocha",
        "LULULU Artesanal Donuts Tribunal",
        "Panaix Bakery & Coffee Montecarmelo",
        "Rousquillas Bakery Ibiza"
    )

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_open_street_map)

        Log.d(TAG, "onCreate: The activity OpenMaps is being created.")

        shopViewModel.shopsNearCoordinates.observe(this) { nearbyShops ->
            shops = nearbyShops ?: emptyList()
            updateShopDetails()
        }

        addressViewModel.nearAddresses.observe(this) { nearbyAddresses ->
            addresses = nearbyAddresses ?: emptyList()
            updateShopDetails()
        }

        coordinateViewModel.nearCoordinates.observe(this) { nearbyCoordinates ->
            coordinates = nearbyCoordinates ?: emptyList()
            updateShopDetails()
        }

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location", Location::class.java)
        if (location != null) {
            Log.i(
                TAG,
                "onCreate: Location[" + location.altitude + "][" + location.latitude + "][" + location.longitude + "]["
            )
            Configuration.getInstance()
                .load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))
            map = findViewById(R.id.map)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.controller.setZoom(18.0)
            val startPoint = GeoPoint(location.latitude, location.longitude)
            //val startPoint = GeoPoint(40.416775, -3.703790) in case you want to test it mannualy
            map.controller.setCenter(startPoint)
            addMarkers(map, gymkhanaCoords, gymkhanaNames)
            addMarkersAndRoute(map, gymkhanaCoords, gymkhanaNames)
            addMarker(startPoint, "My current location")
        };
    }

    private fun updateShopDetails() {
        val shopDetails = shops.map { shop ->
            ShopDetail(
                shop = shop,
                address = shop.addressId?.let { addresses[it] },
                coordinate = shop.locationId?.let { coordinates[it] }
            )
        }
        // Now you have a list of ShopDetail objects, you can update your UI here
    }

    private fun updateNearbyStores(location: Location) {
        // Convert Location to your Coordinate class (if necessary)
        val coordinate = Coordinate(latitude=location.latitude, longitude=location.longitude)

        val radius = 5000 // Define the radius in meters

        shopViewModel.shopsNearCoordinates.observe(this) { shops ->
            if (shops != null) {
                addShopMarkers(shops)
            }
        }
    }

    private fun addShopMarkers(shops: List<Shop>) {
        for (shop in shops) {
            shop.locationId?.let { locationId ->
                coordinateViewModel.nearCoordinates.observe(this) { coordinate ->
                    coordinate?.let {
                        val geoPoint = GeoPoint(0, 0)
                        addMarker(geoPoint, shop.name)
                    }
                }
            }
        }
        map.invalidate() // Refresh the map to display the new markers
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

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set the custom icon for the marker
        marker.icon = ContextCompat.getDrawable(this, R.drawable.current_location_marker)

        map.overlays.add(marker)
        map.invalidate() // Reload map
    }

    fun addMarkers(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Marker at ${locationsNames.get(locationsCoords.indexOf(location))} ${location.latitude}, ${location.longitude}"
            marker.icon = ContextCompat.getDrawable(this, R.drawable.shop_marker)
            mapView.overlays.add(marker)
        }
        mapView.invalidate() // Refresh the map to display the new markers
    }

    private fun addMarkersAndRoute(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        if (locationsCoords.size != locationsNames.size) {
            Log.e("addMarkersAndRoute", "Locations and names lists must have the same number of items.")
            return
        }
        val route = Polyline()
        route.setPoints(locationsCoords)
        route.color = ContextCompat.getColor(this, R.color.teal_700)
        mapView.overlays.add(route)
        for (location in locationsCoords) {
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            val locationIndex = locationsCoords.indexOf(location)
            marker.title = "Marker at ${locationsNames[locationIndex]} ${location.latitude}, ${location.longitude}"
            marker.icon = ContextCompat.getDrawable(this, R.drawable.donut_marker)
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    data class ShopDetail(
        val shop: Shop,
        val address: Address?,
        val coordinate: Coordinate?
    )
}