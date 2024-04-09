package com.example.mad_2024_app.Network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassAPIService {
    @GET("interpreter")
    fun query(@Query("data") data: String): Call<String>
}