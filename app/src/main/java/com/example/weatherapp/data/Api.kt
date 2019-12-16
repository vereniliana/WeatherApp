package com.example.weatherapp.data

import com.example.weatherapp.data.model.DataResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("/data/2.5/weather")
    fun getDataByCoord(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") apiKey: String): Call<DataResponse>
}