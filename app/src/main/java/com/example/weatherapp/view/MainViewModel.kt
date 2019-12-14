package com.example.weatherapp.view

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import java.io.IOException

class MainViewModel : ViewModel() {

    var latitude = MutableLiveData<String>()
    var longitude =  MutableLiveData<String>()

    val apiKey = "6a1b204d6b4c85ac96ec111a13aa0ecd"

    @Bindable
    fun setLat(latValue : Double) {
        latitude.value = latValue.toString()
    }

    fun setLong(longValue : Double) {
        longitude.value = longValue.toString()
    }

    fun loadData() {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${latitude.value.toString()}&lon=${longitude.value.toString()}&APPID=$apiKey"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("MainViewModel", "FAILED")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("MainViewModel", body)
            }

        })
    }
}