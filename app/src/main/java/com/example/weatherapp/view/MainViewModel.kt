package com.example.weatherapp.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.R
import com.google.gson.Gson
import com.squareup.picasso.Picasso
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
    var imageUrl = MutableLiveData<String>("")
    var toastText = MutableLiveData<String>()

    val apiKey = "6a1b204d6b4c85ac96ec111a13aa0ecd"

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
                toastText.value = "Load data failed"
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = JSONObject(body)

                activity.runOnUiThread {
                    if (json.getString("cod") == "200") {
                        val gson = Gson()
                        val resp = gson.fromJson(body,com.example.weatherapp.data.model.Response::class.java)
                        location.value = resp.location
                        weather.value = resp.weatherList[0].main
                        description.value = resp.weatherList[0].description
                        temperature.value = convertKelvinToCelsius(resp.info.temperature).toString() + activity.getString(
                                R.string.unit_temp
                            )
                        pressure.value = resp.info.pressure + activity.getString(R.string.unit_pressure)
                        humidity.value = resp.info.humidity + activity.getString(R.string.unit_humidity)
                        imageUrl.value = "http://openweathermap.org/img/wn/${resp.weatherList[0].icon}@2x.png"

                    } else {
                        toastText.value = json.getString("message")
                    }
                }
            }

        })
    }

    fun convertKelvinToCelsius(k: String): Double {
        val result = (k.toFloat() - 273.15)
        return "%.2f".format(result).toDouble()
    }

    companion object DataBindingAdapter {
        @BindingAdapter("bind:imageUrl")
        @JvmStatic
        fun loadImage(imageView: ImageView, imageUrl: String) {
            if (imageUrl.isNotEmpty() && imageUrl.isNotBlank()) {
                Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView)
            }
        }
    }

}