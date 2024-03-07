package com.example.mad_2024_app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mad_2024_app.DAOs.ShopDAO
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.database.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DAOUnitTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }
    @Test
    fun insertAndRetrieveShop() {
        val dao = database.shopDao()
        val shop = Shop(name = "Test Shop", description = "Test Description")
        dao.insert(shop)

        val retrievedShop = dao.getShopById(1)
        assertEquals(shop.name, retrievedShop.name)

        dao.delete(retrievedShop)
    }

    @Test
    fun insertAndRetrieveUser() {
        val dao = database.userDao()
        val user = User(username = "Test User", email = "martini@chinchong.com", uuid = "5de7c711-8c70-4515-853a-dcaf67300183")
        dao.insert(user)

        val retrievedUser = dao.getUserById(1)
        assertEquals(user.username, retrievedUser.username)

        dao.delete(retrievedUser)
    }
}
