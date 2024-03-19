import android.content.Context
import com.example.mad_2024_app.App
import com.example.mad_2024_app.AppDatabase
import com.example.mad_2024_app.repositories.AddressRepository
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
    }
}