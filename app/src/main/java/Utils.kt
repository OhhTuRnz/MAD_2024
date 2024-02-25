import android.content.Context
import android.location.Location
import java.io.File
import java.io.IOException

class Utils {
    companion object {
        fun writeLocationToCSV(context: Context, location: Location) {
            val fileName = "locations.csv"
            val file = File(context.filesDir, fileName)
            val isNewFile = file.createNewFile() // Returns true if the file is created

            try {
                context.openFileOutput(fileName, Context.MODE_APPEND).use {
                    // Write the header if the file is new
                    if (isNewFile) {
                        it.write("Latitude,Longitude,Altitude\n".toByteArray())
                    }
                    // Append location data
                    val fileContents = "${location.latitude},${location.longitude},${location.altitude}\n"
                    it.write(fileContents.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}