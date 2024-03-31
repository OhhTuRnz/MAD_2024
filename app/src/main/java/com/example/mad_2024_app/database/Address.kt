package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Coordinate::class,
            parentColumns = ["coordinateId"],
            childColumns = ["coordinateId"])
    ],
    indices = [Index(value = ["coordinateId"], unique = true)]
)
data class Address(
    @PrimaryKey(autoGenerate = true) val addressId: Int = 0,
    val street: String,
    val city: String? = null,
    val zipCode: Int? = null,
    val number: Int? = null,
    val country: String? = null,
    val coordinateId: Int
)
