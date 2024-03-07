package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = Coordinate::class,
        parentColumns = ["coordinateId"],
        childColumns = ["location"]),
    ForeignKey(entity = Address::class,
        parentColumns = ["addressId"],
        childColumns = ["address"])])
data class Shop (
    @PrimaryKey(autoGenerate = true) val shopId: Int = 0,
    val name : String,
    val description : String,
    val address: Int? = null,
    val location : Int? = null
)