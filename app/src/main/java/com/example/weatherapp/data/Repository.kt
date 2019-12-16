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
    val progressState = MutableLiveData<Boolean>()
    var retrofit = WeatherService()

    fun getDataByCoord(lat: String, lon: String) {

        val api = retrofit.createService(Api::class.java)

        progressState.postValue(true)
        api.getDataByCoord(lat, lon, apiKey).enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    dataResponse.postValue(response.body())
                } else {
                    errorMsg.postValue(response.message())
                }
                progressState.postValue(false)
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                dataResponse.postValue(null)
                progressState.postValue(false)
            }
        })
    }

    fun getDataByCity(city: String) {
        val api = retrofit.createService(Api::class.java)

        progressState.postValue(true)
        api.getDataByCity(city, apiKey).enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    dataResponse.postValue(response.body())
                } else {
                    errorMsg.postValue(response.message())
                }
                progressState.postValue(false)
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                dataResponse.postValue(null)
                progressState.postValue(false)
            }
        })
    }
}