package com.example.weatherapp.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.R
import com.example.weatherapp.data.Repository
import com.squareup.picasso.Picasso

class MainViewModel : ViewModel() {

    private var latitude: String = ""
    private var longitude: String = ""
    private val repository = Repository()

    val dataResponse = repository.dataResponse
    val errorMsg = repository.errorMsg
    val progressState = repository.progressState
    var imageUrl = MutableLiveData<String>("")
    var city : String = ""

    fun setLat(latValue : Double) {
        latitude = latValue.toString()
    }

    fun setLong(longValue : Double) {
        longitude = longValue.toString()
    }

    fun loadDataByCoord() {
        repository.getDataByCoord(latitude, longitude)
    }

    fun loadDataByCity() {
        repository.getDataByCity(city)
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