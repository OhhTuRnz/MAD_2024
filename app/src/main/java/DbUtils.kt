import android.content.Context
import com.example.mad_2024_app.App
import com.example.mad_2024_app.AppDatabase
import com.example.mad_2024_app.repositories.AddressRepository
import com.example.mad_2024_app.repositories.CoordinateRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.repositories.FavoriteShopsRepository
import com.example.mad_2024_app.repositories.ShopRepository
import com.example.mad_2024_app.repositories.UserRepository
import com.google.common.cache.Cache

class DbUtils {

    companion object {
        private fun getCache(context: Context) : Cache<String, Any> {
            return (context.applicationContext as App).cache
        }
        private fun getDatabase(context: Context): AppDatabase {
            // Assuming 'AppDatabase' is your Room database class and 'getDatabase' is a static method
            return AppDatabase.getDatabase(context)
        }

        // Function to get UserRepository
        fun getUserRepository(context: Context): UserRepository {
            val database = getDatabase(context)
            val userDao = database.userDao()
            return UserRepository(userDao, getCache(context))
        }

        fun getShopRepository(context: Context): ShopRepository {
            val database = getDatabase(context)
            val shopDao = database.shopDao()
            return ShopRepository(shopDao, getCache(context))
        }

        fun getAddressRepository(context: Context): AddressRepository {
            val database = getDatabase(context)
            val addressDao = database.addressDao()
            return AddressRepository(addressDao, getCache(context))
        }

        fun getCoordinateRepository(context: Context): CoordinateRepository {
            val database = getDatabase(context)
            val coordinateDao = database.coordinateDao()  // Replace with your actual method name
            return CoordinateRepository(coordinateDao, getCache(context))
        }

        fun getFavoriteShopsRepository(context: Context): FavoriteShopsRepository {
            val database = getDatabase(context)
            val favoriteShopsDao = database.favoriteShopsDao()  // Replace with your actual method name
            return FavoriteShopsRepository(favoriteShopsDao, getCache(context))
        }

        fun getFavoriteDonutsRepository(context: Context): FavoriteDonutsRepository {
            val database = getDatabase(context)
            val favoriteDonutsDao = database.favoriteDonutsDao()
            return FavoriteDonutsRepository(favoriteDonutsDao, getCache(context))
        }
    }
}