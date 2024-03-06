package com.example.mad_2024_app.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = Coordinate::class,
        parentColumns = ["id"],
        childColumns = ["location"]),
    ForeignKey(entity = Address::class,
        parentColumns = ["id"],
        childColumns = ["addressId"])])
data class Shop (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name : String,
    val description : String,
    val addressId: String,
    val location : Int
)