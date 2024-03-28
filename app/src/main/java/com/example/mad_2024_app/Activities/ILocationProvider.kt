package com.example.mad_2024_app.Activities

import android.location.Location

interface ILocationProvider {
    fun getLatestLocation(): Location?
}