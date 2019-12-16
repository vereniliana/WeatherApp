package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class DataResponse(
    @SerializedName("cod") val code: String,
    @SerializedName("name") val location: String,
    @SerializedName("weather") val weatherList: List<Weather>,
    @SerializedName("main") val info: Info
)