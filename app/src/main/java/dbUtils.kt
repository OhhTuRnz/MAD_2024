import android.content.Context
import com.example.mad_2024_app.App
import com.example.mad_2024_app.AppDatabase
import com.example.mad_2024_app.repositories.UserRepository

class dbUtils {

    companion object {
        // Function to get the database instance
        private fun getDatabase(context: Context): AppDatabase {
            // Assuming 'AppDatabase' is your Room database class and 'getDatabase' is a static method
            return AppDatabase.getDatabase(context)
        }

        // Function to get UserRepository
        fun getUserRepository(context: Context): UserRepository {
            val database = getDatabase(context)
            val userDao = database.userDao()
            return UserRepository(userDao)
        }
    }
}