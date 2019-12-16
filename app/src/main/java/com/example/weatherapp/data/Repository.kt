package com.example.weatherapp.data

import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.model.DataResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit

class Repository {

    val apiKey = "6a1b204d6b4c85ac96ec111a13aa0ecd"
    val dataResponse = MutableLiveData<DataResponse>()
    val errorMsg = MutableLiveData<String>()

    fun getDataByCoord(lat: String, lon: String) {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(Api::class.java)

        api.getDataByCoord(lat, lon, apiKey).enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    dataResponse.value = response.body()
                } else {
                    errorMsg.value = response.message()
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                dataResponse.value = null
            }
        })
    }

    fun getDataByCity(city: String) {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(Api::class.java)

        api.getDataByCity(city, apiKey).enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    dataResponse.value = response.body()
                } else {
                    errorMsg.value = response.message()
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                dataResponse.value = null
            }
        })
    }
}