package com.example.weatherapp.data

import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.model.DataResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Repository {

    val apiKey = "6a1b204d6b4c85ac96ec111a13aa0ecd"
    val dataResponse = MutableLiveData<DataResponse>()
    val errorMsg = MutableLiveData<String>()
    var retrofit = WeatherService()

    fun getDataByCoord(lat: String, lon: String) {

        val api = retrofit.createService(Api::class.java)

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
        val api = retrofit.createService(Api::class.java)

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