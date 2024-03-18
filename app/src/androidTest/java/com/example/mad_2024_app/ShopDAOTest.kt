package com.example.mad_2024_app.DAOs

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mad_2024_app.AppDatabase
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.database.Shop
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class ShopDAOTest {

    private lateinit var shopDao: ShopDAO
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        shopDao = db.shopDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testGetShopsWithinBounds() = runBlocking {
        // Insert test data
        val coordinate1 = Coordinate(latitude=12.345, longitude=67.890)
        val coordinate2 = Coordinate(latitude=12.400, longitude=67.950)
        val id1 = db.coordinateDao().insert(coordinate1)
        val id2 = db.coordinateDao().insert(coordinate2)

        val shop1 = Shop(name="Shop Inside Radius", description="Tonto", locationId=id1.toInt())
        val shop2 = Shop(name="Shop Outside Radius", description="Tonto", locationId=id2.toInt())
        shopDao.insert(shop1)
        shopDao.insert(shop2)

        // Perform query
        val shopsLiveData = shopDao.getShopsWithinBounds(12.300, 12.500, 67.800, 68.000)

        // Create a TestLiveDataObserver to observe the LiveData
        val observer = TestLiveDataObserver<List<Shop>>()

        // Observe the LiveData using the TestLiveDataObserver
        shopsLiveData.observeForever(observer)

        // Wait for LiveData to emit a value
        val shops = observer.awaitValue()

        // Check if shops list is not null and contains expected data
        assertNotNull(shops)
        assertTrue(shops?.size == 1)
        assertEquals("Shop Inside Radius", shops?.get(0)?.name)

        // Print the shops found
        shops?.forEach { shop ->
            println("Shop found: ${shop.name}")
        }

        // Clean up
        shopsLiveData.removeObserver(observer)
    }

    class TestLiveDataObserver<T> : Observer<T> {
        private val observedValues = mutableListOf<T?>()
        private val latch = CountDownLatch(1)

        override fun onChanged(value: T) {
            observedValues.add(value)
            latch.countDown()
        }

        fun awaitValue(): T? {
            latch.await()
            return observedValues.lastOrNull()
        }
    }
}
