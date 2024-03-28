package com.example.mad_2024_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["username"], unique = true), Index(value = ["email"], unique = true), Index(value = ["uuid"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "username") val username: String? = null,
    @ColumnInfo(name = "email") val email: String? = null,
    val iconPath: String? = null, // This could be a path or a resource identifier,
    @ColumnInfo(name = "uuid") val uuid: String,
    val homeLatitude : Double? = null,
    val homeLongitude : Double? = null
)