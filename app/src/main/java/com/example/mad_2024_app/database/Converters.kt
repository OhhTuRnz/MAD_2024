package com.example.mad_2024_app.database

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromCoordinate(coordinate: Coordinate): String {
        // Convert Coordinate object to JSON String
        return Gson().toJson(coordinate)
    }

    @TypeConverter
    fun toCoordinate(coordinateString: String): Coordinate {
        // Convert JSON String back to Coordinate object
        return Gson().fromJson(coordinateString, Coordinate::class.java)
    }
}