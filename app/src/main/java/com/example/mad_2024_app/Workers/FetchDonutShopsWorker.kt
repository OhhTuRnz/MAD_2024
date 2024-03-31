package com.example.mad_2024_app.Workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mad_2024_app.App
import com.example.mad_2024_app.Network.OverpassAPIService
import com.example.mad_2024_app.RepositoryProvider
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject

class FetchDonutShopsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    ): CoroutineWorker(appContext, workerParams) {

    private val TAG = "FetchDonutShopsWorker"

    private val shopRepo = RepositoryProvider.getShopRepository()
    private val coordinateRepo = RepositoryProvider.getCoordinateRepository()
    private val addressRepo = RepositoryProvider.getAddressRepository()

    override suspend fun doWork(): Result {
        return try {
            // Fetch location
            val (latitude, longitude) = getCurrentLocation()

            if (latitude == Double.POSITIVE_INFINITY || longitude == Double.POSITIVE_INFINITY) {
                return Result.failure()
            }

            // Construct the Overpass API query
            val query = """
                [out:json];
                (
                  node(around:5000, $latitude, $longitude)["amenity"="cafe"][name]["addr:street"];
                );
                out body;
                out tags;
            """

            // Access Retrofit for Overpass API
            val retrofit = App.getOverpassRetrofit(applicationContext)
            val overpassApiService = retrofit.create(OverpassAPIService::class.java)

            // Execute the API call
            val response = overpassApiService.query(query).execute()

            if (response.isSuccessful) {
                val shopsData = response.body().toString()
                val json = JSONObject(shopsData)
                val elements = json.getJSONArray("elements")

                for (i in 0 until elements.length()) {
                    Log.d(TAG, "Element number $i being processed")
                    val element = elements.getJSONObject(i)
                    if (element.has("tags")) {
                        val tags = element.getJSONObject("tags")

                        val description = ""

                        // Extract shop details
                        val name = tags.optString("name", "")

                        // Extract additional details
                        val cuisine = tags.optString("cuisine", null)
                        val wifi = tags.optString("internet_access", null)
                        val phone = tags.optString("phone", null)
                        val outdoorSeating = tags.optString("outdoor_seating", null)
                        val openingHours = tags.optString("opening_hours", null)

                        Log.d(TAG, "cuisine: $cuisine wifi: $wifi phone: $phone outdoorSeating: $outdoorSeating openingHours: $openingHours")

                        // You can create a combined description or a separate field for these additional details
                        val combinedDescription = buildString {
                            if (!cuisine.isNullOrEmpty()) {
                                append("\nCuisine: $cuisine")
                            }
                            if (!outdoorSeating.isNullOrEmpty()) {
                                append("\nIndoor Seating: $outdoorSeating")
                            }
                            if (!wifi.isNullOrEmpty()) {
                                append("\nWiFi: $wifi")
                            }
                            if (!phone.isNullOrEmpty()) {
                                append("\nPhone: $phone")
                            }
                            if (!openingHours.isNullOrEmpty()) {
                                append("\nOpening Hours: $openingHours")
                            }
                            if (isEmpty()) {
                                append("No additional information available")
                            }
                        }


                        // Extract address details
                        val street = tags.optString("addr:street", "")
                        val city = tags.optString("addr:city", "")
                        val zipCode = tags.optInt("addr:postcode", 0)
                        val number = tags.optInt("addr:housenumber", 0)
                        val country = tags.optString("addr:country", "")

                        // Extract coordinate details
                        val elementLatitude = element.optDouble("lat", Double.POSITIVE_INFINITY)
                        val elementLongitude = element.optDouble("lon", Double.POSITIVE_INFINITY)

                        if(elementLatitude == Double.POSITIVE_INFINITY || elementLongitude == Double.POSITIVE_INFINITY){
                            Log.d(TAG, "Element $i has no latitude or longitude")
                            continue
                        }

                        Log.d(TAG, "Upserting Coordinate")

                        // Check for existing coordinate
                        val existingCoordinateFlow = coordinateRepo.getCoordinateByLatitudeAndLongitude(elementLatitude, elementLongitude)
                        val existingCoordinate = existingCoordinateFlow.firstOrNull()

                        val coordinateId : Long

                        if (existingCoordinate == null) {
                            // Upsert Coordinate as it doesn't exist
                            coordinateId = coordinateRepo.upsertCoordinate(Coordinate(latitude = elementLatitude, longitude = elementLongitude))
                            if (coordinateId == -1L) {
                                // Handle failure to upsert coordinate
                                Log.d(TAG, "Element $i coordinate upsert failed")
                                continue // Skip to the next iteration
                            }
                        }
                        else{
                            coordinateId = existingCoordinate.coordinateId.toLong()
                        }

                        Log.d(TAG, "Coordinate with latitude: $elementLatitude and longitude $elementLongitude has ID: $coordinateId")
                        val existingAddressFlow = addressRepo.getAddressByLocationId(coordinateId.toInt())
                        val existingShopFlow = shopRepo.getShopByLocationId(coordinateId.toInt())

                        val existingAddress = existingAddressFlow.firstOrNull()
                        val existingShop = existingShopFlow.firstOrNull()

                        val addressId : Long

                        if (existingAddress == null) {
                            addressId = addressRepo.upsertAddress(Address(street = street, city = city, zipCode = zipCode, number = number, country = country, coordinateId = coordinateId.toInt()))
                            if (addressId == -1L) {
                                // Handle failure to insert/update address
                                Log.d(TAG, "Element $i address upsert failed")
                                continue // Skip to the next iteration
                            }
                        }else{
                            addressId = existingAddress.addressId.toLong()
                        }

                        // If the shop does not exist, upsert it with the addressId
                        if (existingShop == null) {
                            Log.d(TAG, "Upserting shop with name: $name and description: $description")
                            val shop = Shop(name = name, description = combinedDescription, addressId = addressId.toInt(), locationId = coordinateId.toInt(), lastAccessed = System.currentTimeMillis())
                            shopRepo.upsert(shop)
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.d(TAG, "Worker failed with exception: $e")
            Result.retry()
        }
    }


    private fun getCurrentLocation(): Pair<Double, Double> {
        val sharedPreferences = applicationContext.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val latitude = sharedPreferences.getString("latestLatitude", null)?.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
        val longitude = sharedPreferences.getString("latestLongitude", null)?.toDoubleOrNull() ?: Double.POSITIVE_INFINITY

        Log.d(TAG, "Worker got : Latitude: $latitude, Longitude: $longitude")
        return Pair(latitude, longitude)
    }
}
