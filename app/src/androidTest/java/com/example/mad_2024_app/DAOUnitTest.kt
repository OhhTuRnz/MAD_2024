package com.example.mad_2024_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.database.Shop
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DAOUnitTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ShopDAO

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.shopDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun test(){
        assertEquals(true, true)
    }
    @Test
    fun insertAndRetrieveShop() {
        val shop = Shop(name = "Test Shop", description = "Test Description")
        dao.insert(shop)

        val retrievedShop = dao.getShopById(1)
        assertEquals(shop.name, retrievedShop.name)

        dao.delete(retrievedShop)
    }
}
