package com.example.weatherapp.view

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainViewModel : ViewModel() {

    lateinit var activity: MainActivity
    var latitude = MutableLiveData<String>()
    var longitude =  MutableLiveData<String>()
    var location = MutableLiveData<String>()
    var weather =  MutableLiveData<String>()
    var description =  MutableLiveData<String>()
    var temperature = MutableLiveData<String>()
    var pressure =  MutableLiveData<String>()
    var humidity = MutableLiveData<String>()

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
                val json = JSONObject(body)

                if (json.getString("cod") == "200") {
                    val gson = Gson()
                    val resp = gson.fromJson(body, com.example.weatherapp.data.model.Response::class.java)
                    activity.runOnUiThread {
                        location.value = resp.location
                        weather.value = resp.weatherList[0].main
                        description.value = resp.weatherList[0].description
                        temperature.value = resp.info.temperature
                        pressure.value = resp.info.pressure
                        humidity.value = resp.info.humidity
                    }
                } else {
                    Log.d("MainViewMode", json.getString("load data failed"))
                }
            }

        })
    }
}