package com.example.weatherapp.data

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit

class WeatherService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T>createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}