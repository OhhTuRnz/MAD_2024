package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Coordinate::class,
            parentColumns = ["coordinateId"],
            childColumns = ["coordinateId"])
    ]
)
data class Address(
    @PrimaryKey(autoGenerate = true) val addressId: Int,
    val street: String,
    val city: String,
    val zipCode: String? = null,
    val country: String,
    val coordinateId: Int? = null
)
