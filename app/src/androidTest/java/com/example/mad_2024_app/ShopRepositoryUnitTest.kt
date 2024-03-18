import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mad_2024_app.AppDatabase
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.ShopRepository
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ShopRepositoryUnitTest {

    private lateinit var database: AppDatabase
    private lateinit var shopRepository: ShopRepository
    private lateinit var cache: Cache<String, Any>

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build()
        shopRepository = ShopRepository(database.shopDao(), cache)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getAllNearbyShopsTest() {
        runBlocking {
            val testLocation = Coordinate(latitude = 12.345, longitude = 67.890) // Test location
            val radius = 5000 // Test radius in meters

            // Coordinate inside test radius
            val insideCoordinate =
                Coordinate(latitude = 12.346, longitude = 67.891) // Very close to testLocation

            // Coordinate outside test radius
            val outsideCoordinate =
                Coordinate(latitude = 12.400, longitude = 67.950) // Far from testLocation

            val coordId1 = database.coordinateDao().insert(insideCoordinate)
            val coordId2 = database.coordinateDao().insert(outsideCoordinate)

            val shop1 = Shop(
                name = "Shop Inside Radius",
                description = "Tonto",
                locationId = coordId1.toInt()
            )
            val shop2 = Shop(
                name = "Shop Outside Radius",
                description = "Tonto",
                locationId = coordId2.toInt()
            )

            database.shopDao().insert(shop1)
            database.shopDao().insert(shop2)

            // Perform the test within a coroutine
            launch {
                // Retrieve the LiveData value asynchronously
                val resultLiveData = shopRepository.getAllShopsNearCoordinates(testLocation, radius)

                // Await the result
                val shops = resultLiveData.value

                // Print the list of shops
                println("List of shops:")
                shops?.forEach { shop ->
                    println("Name: ${shop.name}, Description: ${shop.description}")
                }

                // Assertions
                if (shops != null) {
                    assertTrue(shops.any { it.name == "Shop Inside Radius" })
                    assertFalse(shops.any { it.name == "Shop Outside Radius" })
                } else {
                    assertTrue("nothing", true == false)
                }

                // Cleanup
                database.shopDao().delete(shop1)
                database.shopDao().delete(shop2)
            }
        }
    }
}
