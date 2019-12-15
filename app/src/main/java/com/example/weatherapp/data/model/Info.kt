package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class Info(
    @SerializedName("temp") val temperature: String,
    val pressure: String,
    val humidity: String
)