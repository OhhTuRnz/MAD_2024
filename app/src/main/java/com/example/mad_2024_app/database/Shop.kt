package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = Coordinate::class,
        parentColumns = ["coordinateId"],
        childColumns = ["locationId"]),
    ForeignKey(entity = Address::class,
        parentColumns = ["addressId"],
        childColumns = ["addressId"])])
data class Shop (
    @PrimaryKey(autoGenerate = true) val shopId: Int = 0,
    val name : String,
    val description : String,
    val addressId: Int? = null,
    val locationId : Int? = null
)