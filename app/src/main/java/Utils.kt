import android.content.Context
import android.location.Location
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mad_2024_app.R
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
                    val fileContents =
                        "${location.latitude},${location.longitude},${location.altitude}\n"
                    it.write(fileContents.toByteArray())
                }
                // Manage file exception
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun saveCoordinatesToFile(latitude: Double, longitude: Double, filesDir: File) {
            val fileName = "gps_coordinates.csv"
            val file = File(filesDir, fileName)
            val timestamp = System.currentTimeMillis()
            file.appendText("$timestamp;$latitude;$longitude\n")
        }

        fun askForUserIdentifier(context: Context) {
            val input = EditText(context)
            AlertDialog.Builder(context)
                .setTitle("Enter Your Username")
                .setIcon(R.mipmap.ic_launcher)
                .setView(input)
                .setPositiveButton("Save") { dialog, which ->
                    var userInput = input.text.toString()
                    if (userInput.isBlank()) {
                        userInput = "Banana"
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}